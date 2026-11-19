import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type {
Retailer,
RetailerProduct,
RetailerDashboard,
RetailerOrder,
RetailerAnalytics
} from '@/types';
import { retailerApi } from '@/services/retailer-api';

/**
 * Retailer store state interface
 */
interface RetailerState {
  // State
  currentRetailer: Retailer | null;
  products: RetailerProduct[];
  orders: RetailerOrder[];
  dashboardData: RetailerDashboard | null;
  analytics: RetailerAnalytics | null;
  
  // Loading states
  isLoadingRetailer: boolean;
  isLoadingProducts: boolean;
  isLoadingOrders: boolean;
  isLoadingDashboard: boolean;
  isLoadingAnalytics: boolean;
  
  // Error states
  error: string | null;

  // Actions
  setRetailer: (retailer: Retailer | null) => void;
  loadRetailer: (userId: number) => Promise<void>;
  loadProducts: (retailerId: number, params?: {
    isActive?: boolean;
    searchTerm?: string;
    page?: number;
    size?: number;
  }) => Promise<void>;
  loadOrders: (retailerId: number, params?: {
    status?: string;
    page?: number;
    size?: number;
  }) => Promise<void>;
  loadDashboard: () => Promise<void>;
  loadAnalytics: (retailerId: number, params?: {
    startDate?: string;
    endDate?: string;
  }) => Promise<void>;
  updateProduct: (retailerId: number, productRetailerId: number, data: {
    price?: number;
    stockQuantity?: number;
    isActive?: boolean;
  }) => Promise<void>;
  clearError: () => void;
}

/**
 * Retailer store using Zustand with persistence
 * 
 * Features:
 * - Persists retailer info to localStorage
 * - Manages retailer state, products, orders, dashboard, and analytics
 * - Provides actions for loading and updating data
 * - Includes loading and error states for all async operations
 */
export const useRetailerStore = create<RetailerState>()(
  persist(
    (set, get) => ({
      // Initial state
      currentRetailer: null,
      products: [],
      orders: [],
      dashboardData: null,
      analytics: null,
      
      isLoadingRetailer: false,
      isLoadingProducts: false,
      isLoadingOrders: false,
      isLoadingDashboard: false,
      isLoadingAnalytics: false,
      
      error: null,

      /**
       * Set retailer directly
       */
      setRetailer: (retailer: Retailer | null) => {
        set({ currentRetailer: retailer });
      },

      /**
       * Load retailer by user ID
       */
      loadRetailer: async (userId: number) => {
        set({ isLoadingRetailer: true, error: null });
        try {
          const retailer = await retailerApi.getRetailerByUserId(userId);
          if (retailer) {
            set({ currentRetailer: retailer, isLoadingRetailer: false });
          } else {
            set({ 
              currentRetailer: null, 
              isLoadingRetailer: false,
              error: 'Retailer not found'
            });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'Failed to load retailer';
          set({
            error: errorMessage,
            isLoadingRetailer: false,
          });
        }
      },

      /**
       * Load retailer products
       */
      loadProducts: async (retailerId: number, params?: {
        isActive?: boolean;
        searchTerm?: string;
        page?: number;
        size?: number;
      }) => {
        set({ isLoadingProducts: true, error: null });
        try {
          const response = await retailerApi.getProducts(retailerId, params);
          if (response) {
            set({ 
              products: response.content, 
              isLoadingProducts: false 
            });
          } else {
            set({ 
              products: [], 
              isLoadingProducts: false 
            });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'Failed to load products';
          set({
            error: errorMessage,
            isLoadingProducts: false,
          });
        }
      },

      /**
       * Load retailer orders
       */
      loadOrders: async (retailerId: number, params?: {
        status?: string;
        page?: number;
        size?: number;
      }) => {
        set({ isLoadingOrders: true, error: null });
        try {
          const response = await retailerApi.getOrders(retailerId, params);
          if (response) {
            set({ 
              orders: response.content, 
              isLoadingOrders: false 
            });
          } else {
            set({ 
              orders: [], 
              isLoadingOrders: false 
            });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'Failed to load orders';
          set({
            error: errorMessage,
            isLoadingOrders: false,
          });
        }
      },

      /**
       * Load retailer dashboard
       */
      loadDashboard: async () => {
        set({ isLoadingDashboard: true, error: null });
        try {
          const dashboard = await retailerApi.getDashboard();
          if (dashboard) {
            set({ 
              dashboardData: dashboard, 
              isLoadingDashboard: false 
            });
          } else {
            set({ 
              dashboardData: null, 
              isLoadingDashboard: false 
            });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'Failed to load dashboard';
          set({
            error: errorMessage,
            isLoadingDashboard: false,
          });
        }
      },

      /**
       * Load retailer analytics
       */
      loadAnalytics: async (retailerId: number, params?: {
        startDate?: string;
        endDate?: string;
      }) => {
        set({ isLoadingAnalytics: true, error: null });
        try {
          const analytics = await retailerApi.getAnalytics(retailerId, params);
          if (analytics) {
            set({ 
              analytics, 
              isLoadingAnalytics: false 
            });
          } else {
            set({ 
              analytics: null, 
              isLoadingAnalytics: false 
            });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'Failed to load analytics';
          set({
            error: errorMessage,
            isLoadingAnalytics: false,
          });
        }
      },

      /**
       * Update product
       */
      updateProduct: async (
        retailerId: number,
        productRetailerId: number,
        data: {
          price?: number;
          stockQuantity?: number;
          isActive?: boolean;
        }
      ) => {
        set({ isLoadingProducts: true, error: null });
        try {
          if (data.price !== undefined) {
            await retailerApi.updateProductPrice(retailerId, productRetailerId, data.price);
          }
          
          if (data.stockQuantity !== undefined) {
            await retailerApi.updateStock(retailerId, productRetailerId, data.stockQuantity);
          }
          
          if (data.isActive !== undefined) {
            if (data.isActive) {
              await retailerApi.activateProduct(retailerId, productRetailerId);
            } else {
              await retailerApi.deactivateProduct(retailerId, productRetailerId);
            }
          }
          
          // Reload products to get updated data
          await get().loadProducts(retailerId);
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'Failed to update product';
          set({
            error: errorMessage,
            isLoadingProducts: false,
          });
        }
      },

      /**
       * Clear error state
       */
      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'retailer-storage',
      partialize: (state) => ({
        currentRetailer: state.currentRetailer,
      }),
    }
  )
);

