import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';
import { toast } from '@/hooks/use-toast';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

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
  (config) => {
    const token = localStorage.getItem('accessToken');
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
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await axios.post(`${API_BASE_URL}/v1/auth/refresh`, {
            refreshToken,
          });

          const { accessToken } = response.data.data;
          localStorage.setItem('accessToken', accessToken);
          
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          }

          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, logout user
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

/**
 * Handle API errors and show toast notifications
 */
export const handleApiError = (error: unknown, defaultMessage = 'An error occurred'): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string; error?: string }>;
    
    if (axiosError.response) {
      const message = axiosError.response.data?.message || 
                     axiosError.response.data?.error || 
                     axiosError.message ||
                     defaultMessage;
      
      toast({
        title: 'Error',
        description: message,
        variant: 'destructive',
      });
      
      return message;
    } else if (axiosError.request) {
      const message = 'Network error. Please check your connection.';
      toast({
        title: 'Network Error',
        description: message,
        variant: 'destructive',
      });
      return message;
    }
  }

  const message = error instanceof Error ? error.message : defaultMessage;
  toast({
    title: 'Error',
    description: message,
    variant: 'destructive',
  });
  
  return message;
};

/**
 * Show success toast notification
 */
export const showSuccessToast = (message: string) => {
  toast({
    title: 'Success',
    description: message,
    variant: 'default',
  });
};

export { apiClient };
export default apiClient;
