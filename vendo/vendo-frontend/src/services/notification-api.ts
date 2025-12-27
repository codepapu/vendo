import { apiClient } from '@/lib/api';
import type {
  NotificationResponse,
  NotificationPreferenceResponse,
  NotificationPreferenceRequest,
  NotificationTypePreferenceResponse,
  NotificationTypePreferenceRequest,
  NotificationType,
  PaginatedResponse,
} from '@/types';

/**
 * Notification API Service
 * Handles all notification-related API calls
 */
class NotificationApiService {
  /**
   * Get user notifications with pagination
   */
  async getNotifications(page: number = 0, size: number = 20): Promise<PaginatedResponse<NotificationResponse>> {
    const response = await apiClient.get<{ data: PaginatedResponse<NotificationResponse> }>(
      '/v1/notifications',
      {
        params: { page, size },
      }
    );
    return response.data.data;
  }

  /**
   * Get unread notification count
   */
  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<{ data: number }>('/v1/notifications/unread-count');
    return response.data.data;
  }

  /**
   * Mark notification as read
   */
  async markAsRead(notificationId: number): Promise<NotificationResponse> {
    const response = await apiClient.put<{ data: NotificationResponse }>(
      `/v1/notifications/${notificationId}/read`
    );
    return response.data.data;
  }

  /**
   * Mark all notifications as read
   */
  async markAllAsRead(): Promise<void> {
    await apiClient.put('/v1/notifications/read-all');
  }

  /**
   * Get notification preferences (global and type-specific)
   */
  async getPreferences(): Promise<NotificationPreferenceResponse> {
    const response = await apiClient.get<{ data: NotificationPreferenceResponse }>(
      '/v1/notifications/preferences'
    );
    return response.data.data;
  }

  /**
   * Update global notification preferences
   */
  async updatePreferences(
    preferences: NotificationPreferenceRequest
  ): Promise<NotificationPreferenceResponse> {
    const response = await apiClient.put<{ data: NotificationPreferenceResponse }>(
      '/v1/notifications/preferences',
      preferences
    );
    return response.data.data;
  }

  /**
   * Get preference for a specific notification type
   */
  async getPreferenceByType(
    notificationType: NotificationType
  ): Promise<NotificationTypePreferenceResponse> {
    const response = await apiClient.get<{ data: NotificationTypePreferenceResponse }>(
      `/v1/notifications/preferences/type/${notificationType}`
    );
    return response.data.data;
  }

  /**
   * Update preference for a specific notification type
   */
  async updateTypePreference(
    preference: NotificationTypePreferenceRequest
  ): Promise<NotificationTypePreferenceResponse> {
    const response = await apiClient.put<{ data: NotificationTypePreferenceResponse }>(
      '/v1/notifications/preferences/type',
      preference
    );
    return response.data.data;
  }
}

export const notificationApi = new NotificationApiService();
export default notificationApi;

