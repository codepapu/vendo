import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type { ApiResponse, OrderResponse } from '@/types';

/**
 * Guest order request type
 */
export interface GuestOrderRequest {
  items: Array<{ productId: number; quantity: number; variationId?: number }>;
  shippingAddress: {
    name: string;
    phone: string;
    email: string;
    address: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
  };
  billingAddress: {
    name: string;
    phone: string;
    email: string;
    address: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
  };
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  shippingMethod?: string;
  couponCode?: string;
  notes?: string;
  createAccount?: boolean;
  password?: string;
}

/**
 * Guest order response type
 */
export interface GuestOrderResponse {
  orderId: number;
  orderNumber: string;
  email: string;
  status: string;
  sessionToken: string;
  sessionTokenExpiresAt: string; // ISO date string
  accountCreated: boolean;
  message: string;
  orderDetails: OrderResponse;
}

/**
 * Guest Checkout API service
 * Handles guest checkout for non-authenticated users
 */
class GuestCheckoutApiService {
  /**
   * Process guest checkout
   */
  async processGuestCheckout(data: GuestOrderRequest): Promise<GuestOrderResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<GuestOrderResponse>>(
        '/v1/guest/checkout',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Order created successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to process guest checkout');
      return null;
    }
  }

  /**
   * Get guest order by session token
   */
  async getGuestOrderByToken(sessionToken: string): Promise<OrderResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<OrderResponse>>(
        '/v1/guest/checkout/order',
        { params: { sessionToken } }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch guest order');
      return null;
    }
  }
}

export const guestCheckoutApi = new GuestCheckoutApiService();
export default guestCheckoutApi;
