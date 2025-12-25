import { apiClient } from '@/lib/api';
import type { NotificationResponse, PaginatedResponse } from '@/types';

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
   * Get notification preferences
   */
  async getPreferences(): Promise<any> {
    const response = await apiClient.get<{ data: any }>('/v1/notifications/preferences');
    return response.data.data;
  }

  /**
   * Update notification preferences
   */
  async updatePreferences(preferences: any): Promise<any> {
    const response = await apiClient.put<{ data: any }>('/v1/notifications/preferences', preferences);
    return response.data.data;
  }
}

export const notificationApi = new NotificationApiService();
export default notificationApi;

