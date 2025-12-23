import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  WishlistResponse,
  ApiResponse,
} from '@/types';

/**
 * Wishlist API service
 * Handles all wishlist-related API calls
 */
class WishlistApiService {
  /**
   * Add a product to wishlist
   */
  async addToWishlist(productId: number): Promise<WishlistResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<WishlistResponse>>(
        `/v1/wishlist/products/${productId}`
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Product added to wishlist');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add product to wishlist');
      return null;
    }
  }

  /**
   * Remove a product from wishlist
   */
  async removeFromWishlist(productId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/wishlist/products/${productId}`
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Product removed from wishlist');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to remove product from wishlist');
      return false;
    }
  }

  /**
   * Get all wishlist items for current user
   */
  async getWishlist(): Promise<WishlistResponse[] | null> {
    try {
      const response = await apiClient.get<ApiResponse<WishlistResponse[]>>(
        '/v1/wishlist'
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch wishlist');
      return null;
    }
  }

  /**
   * Clear all wishlist items
   */
  async clearWishlist(): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        '/v1/wishlist'
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Wishlist cleared');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to clear wishlist');
      return false;
    }
  }

  /**
   * Check if a product is in wishlist
   */
  async isInWishlist(productId: number): Promise<boolean> {
    try {
      const response = await apiClient.get<ApiResponse<boolean>>(
        `/v1/wishlist/products/${productId}/check`
      );
      
      if (response.data.success && response.data.data !== undefined) {
        return response.data.data;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to check wishlist status');
      return false;
    }
  }
}

export const wishlistApi = new WishlistApiService();
export default wishlistApi;
