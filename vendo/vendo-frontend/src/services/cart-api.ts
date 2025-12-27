import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type { ApiResponse } from '@/types';

/**
 * Cart item request type
 */
export interface CartItemRequest {
  productId: number;
  quantity: number;
  retailerId?: number; // Optional, for marketplace
  variationId?: number; // Optional, for product variations
}

/**
 * Cart item response type
 */
export interface CartItemResponse {
  id?: number;
  productId: number;
  productName: string;
  productSku: string;
  productImage?: string;
  price: number;
  salePrice?: number;
  quantity: number;
  itemTotal: number;
  variationId?: number;
  retailerId?: number;
  retailerName?: string;
}

/**
 * Cart response type
 */
export interface CartResponse {
  userId?: number;
  cartType?: 'B2C' | 'B2B' | 'MULTI_VENDOR';
  businessAccountId?: number;
  items: CartItemResponse[];
  totalItems: number;
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  updatedAt: string; // ISO date string
}

/**
 * Cart pricing response type (with B2B pricing and retailer breakdown)
 */
export interface CartPricingResponse {
  cartId: number;
  cartType: 'B2C' | 'B2B' | 'MULTI_VENDOR';
  items: CartItemPricing[];
  retailerShipping: Record<number, number>; // retailerId -> shipping cost
  retailerSubtotals: Record<number, number>; // retailerId -> subtotal
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
}

export interface CartItemPricing {
  itemId: number;
  productId: number;
  productName: string;
  retailerId?: number;
  retailerName?: string;
  quantity: number;
  unitPrice: number;
  itemTotal: number;
  isB2BPricing?: boolean;
}

/**
 * Cart API service
 * Handles all cart-related API calls
 */
class CartApiService {
  /**
   * Get current user's cart
   */
  async getCart(): Promise<CartResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<CartResponse>>('/v1/cart');
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch cart');
      return null;
    }
  }

  /**
   * Add item to cart
   */
  async addItemToCart(data: CartItemRequest): Promise<CartResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<CartResponse>>(
        '/v1/cart/items',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Item added to cart');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add item to cart');
      return null;
    }
  }

  /**
   * Update cart item quantity
   */
  async updateCartItem(
    itemId: number,
    data: { quantity: number }
  ): Promise<CartResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<CartResponse>>(
        `/v1/cart/items/${itemId}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Cart updated');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update cart item');
      return null;
    }
  }

  /**
   * Remove item from cart
   */
  async removeCartItem(itemId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/cart/items/${itemId}`
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Item removed from cart');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to remove item from cart');
      return false;
    }
  }

  /**
   * Clear entire cart
   */
  async clearCart(): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>('/v1/cart');
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Cart cleared');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to clear cart');
      return false;
    }
  }

  /**
   * Get cart pricing with B2B pricing applied if applicable
   */
  async getCartPricing(businessAccountId?: number): Promise<CartPricingResponse | null> {
    try {
      const params = businessAccountId ? { businessAccountId } : {};
      const response = await apiClient.get<ApiResponse<CartPricingResponse>>(
        '/v1/cart/pricing',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to get cart pricing');
      return null;
    }
  }
}

export const cartApi = new CartApiService();
export default cartApi;
