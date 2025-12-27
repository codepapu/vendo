import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import {
  BusinessAccountResponse,
  PurchaseOrderResponse,
  InvoiceResponse,
  B2BPricing,
} from '@/types/b2b';
import { b2bApi } from '@/services/b2b-api';

interface B2BState {
  // State
  currentBusinessAccount: BusinessAccountResponse | null;
  purchaseOrders: PurchaseOrderResponse[];
  invoices: InvoiceResponse[];
  pricingCache: Map<string, B2BPricing>;
  loading: boolean;
  error: string | null;

  // Actions
  setBusinessAccount: (account: BusinessAccountResponse | null) => void;
  loadBusinessAccount: () => Promise<void>;
  loadPurchaseOrders: (status?: string) => Promise<void>;
  loadInvoices: (status?: string) => Promise<void>;
  getPricing: (productId: number, quantity: number) => Promise<B2BPricing>;
  clearError: () => void;
}

export const useB2BStore = create<B2BState>()(
  persist(
    (set, get) => ({
      // Initial state
      currentBusinessAccount: null,
      purchaseOrders: [],
      invoices: [],
      pricingCache: new Map(),
      loading: false,
      error: null,

      // Set business account
      setBusinessAccount: (account) => {
        set({ currentBusinessAccount: account });
      },

      // Load business account
      loadBusinessAccount: async () => {
        set({ loading: true, error: null });
        try {
          const account = await b2bApi.getAccount();
          set({ currentBusinessAccount: account, loading: false });
        } catch (error: any) {
          set({
            error: error.message || 'Failed to load business account',
            loading: false,
          });
        }
      },

      // Load purchase orders
      loadPurchaseOrders: async (status?: string) => {
        set({ loading: true, error: null });
        try {
          const response = await b2bApi.getPurchaseOrders(status);
          set({ purchaseOrders: response.content, loading: false });
        } catch (error: any) {
          set({
            error: error.message || 'Failed to load purchase orders',
            loading: false,
          });
        }
      },

      // Load invoices
      loadInvoices: async (status?: string) => {
        set({ loading: true, error: null });
        try {
          const response = await b2bApi.getInvoices(status);
          set({ invoices: response.content, loading: false });
        } catch (error: any) {
          set({
            error: error.message || 'Failed to load invoices',
            loading: false,
          });
        }
      },

      // Get pricing (with cache)
      getPricing: async (productId: number, quantity: number) => {
        const cacheKey = `${productId}-${quantity}`;
        const cached = get().pricingCache.get(cacheKey);
        if (cached) {
          return cached;
        }

        set({ loading: true, error: null });
        try {
          const pricing = await b2bApi.getPricing(productId, quantity);
          const cache = new Map(get().pricingCache);
          cache.set(cacheKey, pricing);
          set({ pricingCache: cache, loading: false });
          return pricing;
        } catch (error: any) {
          set({
            error: error.message || 'Failed to get pricing',
            loading: false,
          });
          throw error;
        }
      },

      // Clear error
      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'b2b-storage',
      partialize: (state) => ({
        currentBusinessAccount: state.currentBusinessAccount,
      }),
    }
  )
);

