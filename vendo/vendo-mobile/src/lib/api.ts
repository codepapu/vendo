import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import Constants from 'expo-constants';
import { Alert } from 'react-native';

const API_BASE_URL =
  Constants.expoConfig?.extra?.apiUrl || 'http://localhost:8080/api';

/**
 * Create axios instance with base configuration
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

// Request interceptor - Add auth token to requests
apiClient.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    // Handle 401 Unauthorized - Token expired
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = await AsyncStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await axios.post(`${API_BASE_URL}/v1/auth/refresh`, {
            refreshToken,
          });

          const { accessToken } = response.data.data;
          await AsyncStorage.setItem('accessToken', accessToken);
          
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          }

          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, logout user
        await AsyncStorage.multiRemove(['accessToken', 'refreshToken']);
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

/**
 * Handle API errors and show alerts
 */
export const handleApiError = (error: unknown, defaultMessage = 'An error occurred'): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string; error?: string }>;
    
    if (axiosError.response) {
      const message = axiosError.response.data?.message || 
                     axiosError.response.data?.error || 
                     axiosError.message ||
                     defaultMessage;
      
      Alert.alert('Error', message);
      return message;
    } else if (axiosError.request) {
      const message = 'Network error. Please check your connection.';
      Alert.alert('Network Error', message);
      return message;
    }
  }

  const message = error instanceof Error ? error.message : defaultMessage;
  Alert.alert('Error', message);
  
  return message;
};

/**
 * Show success alert
 */
export const showSuccessAlert = (message: string, title: string = 'Success') => {
  Alert.alert(title, message);
};

// Re-export shared APIs
import { ApiClient, AuthApi, ProductsApi } from '@vendo/shared';

const sharedApiClient = new ApiClient(API_BASE_URL);
export const authApi = new AuthApi(sharedApiClient);
export const productsApi = new ProductsApi(sharedApiClient);

export { apiClient };
export default apiClient;
