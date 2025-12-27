import { apiClient, handleApiError } from '@/lib/api';
import type {
  AnalyticsDashboard,
  AnalyticsDashboardResponse,
  TrackEventRequest,
  AnalyticsEventType,
  AnalyticsEventProperties,
  ApiResponse,
} from '@/types';

/**
 * Analytics API service
 * Handles analytics event tracking and dashboard data
 */
class AnalyticsApiService {
  /**
   * Track an analytics event
   * Tracks: page views, product views, cart additions, purchases, user behavior
   */
  async trackEvent(
    eventType: AnalyticsEventType | string,
    properties?: AnalyticsEventProperties,
    sessionId?: string
  ): Promise<boolean> {
    try {
      const request: TrackEventRequest = {
        eventType,
        properties,
        sessionId: sessionId || this.getSessionId(),
      };

      await apiClient.post<ApiResponse<void>>(
        '/v1/analytics/track',
        request.properties || {},
        {
          params: {
            eventType: request.eventType,
            sessionId: request.sessionId,
          },
        }
      );

      // Also track in Google Analytics if configured
      this.trackInGoogleAnalytics(eventType, properties);
      
      // Also track in Mixpanel if configured
      this.trackInMixpanel(eventType, properties);

      return true;
    } catch (error) {
      // Don't show error toast for analytics tracking failures
      console.error('Failed to track analytics event:', error);
      return false;
    }
  }

  /**
   * Track page view
   */
  async trackPageView(pagePath: string, pageTitle?: string): Promise<boolean> {
    return this.trackEvent('PAGE_VIEW', {
      pagePath,
      pageTitle: pageTitle || document.title,
    });
  }

  /**
   * Track product view
   */
  async trackProductView(productId: number, productName?: string, categoryId?: number): Promise<boolean> {
    return this.trackEvent('PRODUCT_VIEW', {
      productId,
      productName,
      categoryId,
    });
  }

  /**
   * Track cart addition
   */
  async trackCartAddition(productId: number, quantity: number, price: number): Promise<boolean> {
    return this.trackEvent('CART_ADDITION', {
      productId,
      quantity,
      price,
      totalValue: quantity * price,
    });
  }

  /**
   * Track cart removal
   */
  async trackCartRemoval(productId: number, quantity: number): Promise<boolean> {
    return this.trackEvent('CART_REMOVAL', {
      productId,
      quantity,
    });
  }

  /**
   * Track purchase
   */
  async trackPurchase(
    orderId: number,
    orderTotal: number,
    items: Array<{ productId: number; quantity: number; price: number }>,
    paymentMethod?: string
  ): Promise<boolean> {
    return this.trackEvent('PURCHASE', {
      orderId,
      orderTotal,
      itemCount: items.length,
      items,
      paymentMethod,
      revenue: orderTotal,
    });
  }

  /**
   * Track search
   */
  async trackSearch(query: string, resultCount?: number, filters?: Record<string, any>): Promise<boolean> {
    return this.trackEvent('SEARCH', {
      query,
      resultCount,
      filters,
    });
  }

  /**
   * Track custom event
   */
  async trackCustomEvent(eventName: string, properties?: AnalyticsEventProperties): Promise<boolean> {
    return this.trackEvent('CUSTOM', {
      customEventName: eventName,
      ...properties,
    });
  }

  /**
   * Get analytics dashboard data
   */
  async getDashboard(): Promise<AnalyticsDashboard | null> {
    try {
      const response = await apiClient.get<ApiResponse<AnalyticsDashboard>>(
        '/v1/analytics/dashboard'
      );

      if (response.data.success && response.data.data) {
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch analytics dashboard');
      return null;
    }
  }

  /**
   * Generate analytics report
   */
  async generateAnalyticsReport(params?: {
    startDate?: string;
    endDate?: string;
    eventTypes?: string[];
  }): Promise<AnalyticsDashboard | null> {
    try {
      const response = await apiClient.get<ApiResponse<AnalyticsDashboard>>(
        '/v1/analytics/dashboard',
        { params }
      );

      if (response.data.success && response.data.data) {
        return response.data.data;
      }

      return null;
    } catch (error) {
      handleApiError(error, 'Failed to generate analytics report');
      return null;
    }
  }

  /**
   * Get or create session ID
   */
  private getSessionId(): string {
    let sessionId = sessionStorage.getItem('analytics_session_id');
    if (!sessionId) {
      sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      sessionStorage.setItem('analytics_session_id', sessionId);
    }
    return sessionId;
  }

  /**
   * Track event in Google Analytics (if configured)
   */
  private trackInGoogleAnalytics(eventType: string, properties?: AnalyticsEventProperties): void {
    // Check if Google Analytics is configured
    if (typeof window !== 'undefined' && (window as any).gtag) {
      try {
        (window as any).gtag('event', eventType.toLowerCase().replace(/_/g, '-'), {
          ...properties,
        });
      } catch (error) {
        console.error('Failed to track in Google Analytics:', error);
      }
    }
  }

  /**
   * Track event in Mixpanel (if configured)
   */
  private trackInMixpanel(eventType: string, properties?: AnalyticsEventProperties): void {
    // Check if Mixpanel is configured
    if (typeof window !== 'undefined' && (window as any).mixpanel) {
      try {
        (window as any).mixpanel.track(eventType, properties);
      } catch (error) {
        console.error('Failed to track in Mixpanel:', error);
      }
    }
  }
}

export const analyticsApi = new AnalyticsApiService();
export default analyticsApi;

