import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type {
  UserDto,
  UserProfileResponse,
  RegisterRequest,
  LoginRequest,
} from '@/types';
import { authApi } from '@/services/auth-api';
import { userApi } from '@/services/user-api';

/**
 * Authentication store state interface
 */
interface AuthState {
  // State
  user: UserDto | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<void>;
  updateUser: (user: UserDto | UserProfileResponse) => void;
  clearError: () => void;
  initialize: () => Promise<void>;
}

/**
 * Authentication store using Zustand with persistence
 * 
 * Features:
 * - Persists tokens and user data to localStorage
 * - Manages authentication state
 * - Provides actions for login, logout, register, refreshToken, updateUser
 * - Includes loading and error states
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      /**
       * Login user
       */
      login: async (data: LoginRequest) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.login(data);
          
          if (response) {
            set({
              user: response.user,
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            });
            
            // Store tokens in localStorage for API client
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);
          } else {
            set({
              error: 'Login failed. Please check your credentials.',
              isLoading: false,
            });
          }
        } catch (error: unknown) {
          const errorMessage = error instanceof Error ? error.message : 'Login failed';
          set({
            error: errorMessage,
            isLoading: false,
          });
          throw error;
        }
      },

      /**
       * Register new user
       */
      register: async (data: RegisterRequest) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.register(data);
          
          if (response) {
            set({
              user: response.user,
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            });
            
            // Store tokens in localStorage for API client
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);
          } else {
            set({
              error: 'Registration failed. Please try again.',
              isLoading: false,
            });
          }
        } catch (error: unknown) {
          const errorMessage = error instanceof Error ? error.message : 'Registration failed';
          set({
            error: errorMessage,
            isLoading: false,
          });
          throw error;
        }
      },

      /**
       * Logout user
       */
      logout: async () => {
        set({ isLoading: true });
        try {
          const { refreshToken } = get();
          if (refreshToken) {
            await authApi.logout(refreshToken);
          }
        } catch (error) {
          console.error('Logout error:', error);
          // Continue with logout even if API call fails
        } finally {
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
          
          // Clear tokens from localStorage
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
        }
      },

      /**
       * Refresh access token
       */
      refreshAccessToken: async () => {
        const { refreshToken: currentRefreshToken } = get();
        
        if (!currentRefreshToken) {
          set({ error: 'No refresh token available' });
          return;
        }

        set({ isLoading: true, error: null });
        try {
          const response = await authApi.refreshToken(currentRefreshToken);
          
          if (response) {
            set({
              accessToken: response.accessToken,
              refreshToken: response.refreshToken,
              user: response.user,
              isAuthenticated: true,
              isLoading: false,
              error: null,
            });
            
            // Update tokens in localStorage
            localStorage.setItem('accessToken', response.accessToken);
            localStorage.setItem('refreshToken', response.refreshToken);
          } else {
            // Refresh failed, logout user
            get().logout();
          }
        } catch (error: unknown) {
          const errorMessage = error instanceof Error ? error.message : 'Token refresh failed';
          set({
            error: errorMessage,
            isLoading: false,
          });
          // Refresh failed, logout user
          get().logout();
        }
      },

      /**
       * Update user data in store
       */
      updateUser: (user: UserDto | UserProfileResponse) => {
        // Convert UserProfileResponse to UserDto format if needed
        const userDto: UserDto = {
          id: user.id,
          email: user.email,
          username: user.username,
          firstName: user.firstName,
          lastName: user.lastName,
          superUser: 'superUser' in user ? user.superUser : false,
          isVerified: user.isVerified,
          emailVerifiedAt: user.emailVerifiedAt,
          isActive: user.isActive,
          userType: user.userType,
        };
        
        set({
          user: userDto,
          isAuthenticated: true,
        });
      },

      /**
       * Clear error state
       */
      clearError: () => {
        set({ error: null });
      },

      /**
       * Initialize auth state from localStorage
       * Checks if tokens exist and validates them
       */
      initialize: async () => {
        const { accessToken, refreshToken } = get();
        
        // If we have tokens, try to get user profile
        if (accessToken && refreshToken) {
          set({ isLoading: true });
          try {
            const profile = await userApi.getProfile();
            
            if (profile) {
              // Convert UserProfileResponse to UserDto
              const userDto: UserDto = {
                id: profile.id,
                email: profile.email,
                username: profile.username,
                firstName: profile.firstName,
                lastName: profile.lastName,
                superUser: false, // Not available in UserProfileResponse
                isVerified: profile.isVerified,
                emailVerifiedAt: profile.emailVerifiedAt,
                isActive: profile.isActive,
                userType: profile.userType,
              };
              
              set({
                user: userDto,
                isAuthenticated: true,
                isLoading: false,
              });
            } else {
              // Profile fetch failed, try to refresh token
              await get().refreshAccessToken();
            }
          } catch (error) {
            // If profile fetch fails, try to refresh token
            try {
              await get().refreshAccessToken();
            } catch (refreshError) {
              // Refresh also failed, logout
              set({
                user: null,
                accessToken: null,
                refreshToken: null,
                isAuthenticated: false,
                isLoading: false,
              });
            }
          }
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
