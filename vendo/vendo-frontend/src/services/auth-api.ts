import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  RegisterRequest,
  LoginRequest,
  AuthResponse,
  ApiResponse,
} from '@/types';

/**
 * Authentication API service
 * Handles all authentication-related API calls
 */
class AuthApiService {
  /**
   * Register a new user
   */
  async register(data: RegisterRequest): Promise<AuthResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/v1/auth/register',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Registration successful');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to register');
      return null;
    }
  }

  /**
   * Login user
   */
  async login(data: LoginRequest): Promise<AuthResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/v1/auth/login',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Login successful');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to login');
      return null;
    }
  }

  /**
   * Refresh access token
   */
  async refreshToken(refreshToken: string): Promise<AuthResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        '/v1/auth/refresh',
        { refreshToken }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to refresh token');
      return null;
    }
  }

  /**
   * Logout user
   */
  async logout(refreshToken?: string): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/auth/logout',
        refreshToken ? { refreshToken } : undefined
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Logged out successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to logout');
      return false;
    }
  }

  /**
   * Verify email with token
   */
  async verifyEmail(token: string): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/auth/verify-email',
        { token }
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Email verified successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to verify email');
      return false;
    }
  }

  /**
   * Request password reset
   */
  async forgotPassword(email: string): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/auth/forgot-password',
        { email }
      );
      
      if (response.data.success) {
        showSuccessToast(
          response.data.message || 'Password reset email sent successfully'
        );
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to send password reset email');
      return false;
    }
  }

  /**
   * Reset password with token
   */
  async resetPassword(
    email: string,
    token: string,
    password: string
  ): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/auth/reset-password',
        { email, token, password }
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Password reset successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to reset password');
      return false;
    }
  }

  /**
   * Change password (authenticated user)
   */
  async changePassword(
    currentPassword: string,
    newPassword: string
  ): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/auth/change-password',
        { currentPassword, newPassword }
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Password changed successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to change password');
      return false;
    }
  }

  /**
   * Resend verification email
   */
  async resendVerificationEmail(email: string): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/auth/resend-verification',
        { email }
      );
      
      if (response.data.success) {
        showSuccessToast(
          response.data.message || 'Verification email sent successfully'
        );
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to resend verification email');
      return false;
    }
  }
}

export const authApi = new AuthApiService();
export default authApi;
