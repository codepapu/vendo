import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  UserProfileResponse,
  ApiResponse,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from '@/types';

/**
 * User API service
 * Handles all user profile-related API calls
 */
class UserApiService {
  /**
   * Get current user profile
   */
  async getProfile(): Promise<UserProfileResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<UserProfileResponse>>(
        '/v1/users/profile'
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch profile');
      return null;
    }
  }

  /**
   * Update user profile
   */
  async updateProfile(data: UpdateProfileRequest): Promise<UserProfileResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<UserProfileResponse>>(
        '/v1/users/profile',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Profile updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update profile');
      return null;
    }
  }

  /**
   * Update user password
   */
  async updatePassword(data: ChangePasswordRequest): Promise<boolean> {
    try {
      const response = await apiClient.put<ApiResponse<void>>(
        '/v1/users/profile/password',
        data
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Password updated successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to update password');
      return false;
    }
  }

  /**
   * Upload user avatar
   */
  async uploadAvatar(file: File): Promise<UserProfileResponse | null> {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await apiClient.post<ApiResponse<UserProfileResponse>>(
        '/v1/users/profile/avatar',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Avatar uploaded successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to upload avatar');
      return null;
    }
  }

  /**
   * Delete user account
   */
  async deleteAccount(): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        '/v1/users/profile/account'
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Account deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete account');
      return false;
    }
  }
}

export const userApi = new UserApiService();
export default userApi;
