import { apiClient, handleApiError } from '@/lib/api';

export interface DeviceTokenRequest {
  deviceToken: string;
  platform: 'ios' | 'android';
}

export interface NotificationPreferenceRequest {
  pushEnabled?: boolean;
  orderUpdates?: boolean;
  promotions?: boolean;
  lowStockAlerts?: boolean;
  newProducts?: boolean;
}

export interface NotificationPreferenceResponse {
  pushEnabled: boolean;
  orderUpdates: boolean;
  promotions: boolean;
  lowStockAlerts: boolean;
  newProducts: boolean;
}

class NotificationApiService {
  /**
   * Register device token for push notifications
   */
  async registerDeviceToken(data: DeviceTokenRequest): Promise<boolean> {
    try {
      const response = await apiClient.post<{ success: boolean; message?: string }>(
        '/v1/notifications/device-token',
        data
      );
      return response.data.success;
    } catch (error) {
      handleApiError(error, 'Failed to register device token');
      return false;
    }
  }

  /**
   * Get notification preferences
   */
  async getNotificationPreferences(): Promise<NotificationPreferenceResponse | null> {
    try {
      const response = await apiClient.get<{ success: boolean; data: NotificationPreferenceResponse }>(
        '/v1/notifications/preferences'
      );
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch notification preferences');
      return null;
    }
  }

  /**
   * Update notification preferences
   */
  async updateNotificationPreferences(
    data: NotificationPreferenceRequest
  ): Promise<NotificationPreferenceResponse | null> {
    try {
      const response = await apiClient.put<{ success: boolean; data: NotificationPreferenceResponse }>(
        '/v1/notifications/preferences',
        data
      );
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update notification preferences');
      return null;
    }
  }
}

export const notificationApi = new NotificationApiService();
export default notificationApi;

