import { apiClient, handleApiError, showSuccessAlert } from '@/lib/api';

export interface CartItemRequest {
  productId: number;
  quantity: number;
  retailerId?: number;
  variationId?: number;
}

export interface CartItemResponse {
  productId: number;
  productName: string;
  productSku: string;
  productImage?: string;
  price: number;
  salePrice?: number;
  quantity: number;
  itemTotal: number;
  variationId?: number;
}

export interface CartResponse {
  userId?: number;
  items: CartItemResponse[];
  totalItems: number;
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  updatedAt: string;
}

class CartApiService {
  async getCart(): Promise<CartResponse | null> {
    try {
      const response = await apiClient.get<{ success: boolean; data: CartResponse }>('/v1/cart');
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch cart');
      return null;
    }
  }

  async addItemToCart(data: CartItemRequest): Promise<CartResponse | null> {
    try {
      const response = await apiClient.post<{ success: boolean; data: CartResponse; message?: string }>(
        '/v1/cart/items',
        data
      );
      if (response.data.success && response.data.data) {
        showSuccessAlert(response.data.message || 'Item added to cart');
        return response.data.data;
      }
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add item to cart');
      return null;
    }
  }

  async updateCartItem(itemId: number, data: { quantity: number }): Promise<CartResponse | null> {
    try {
      const response = await apiClient.put<{ success: boolean; data: CartResponse; message?: string }>(
        `/v1/cart/items/${itemId}`,
        data
      );
      if (response.data.success && response.data.data) {
        showSuccessAlert(response.data.message || 'Cart updated');
        return response.data.data;
      }
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update cart item');
      return null;
    }
  }

  async removeCartItem(itemId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<{ success: boolean; message?: string }>(
        `/v1/cart/items/${itemId}`
      );
      if (response.data.success) {
        showSuccessAlert(response.data.message || 'Item removed from cart');
        return true;
      }
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to remove item from cart');
      return false;
    }
  }

  async clearCart(): Promise<boolean> {
    try {
      const response = await apiClient.delete<{ success: boolean; message?: string }>('/v1/cart');
      if (response.data.success) {
        showSuccessAlert(response.data.message || 'Cart cleared');
        return true;
      }
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to clear cart');
      return false;
    }
  }
}

export const cartApi = new CartApiService();
export default cartApi;

