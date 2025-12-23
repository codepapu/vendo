import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  ReviewResponse,
  ApiResponse,
  PaginatedResponse,
  ReviewRequest,
} from '@/types';

/**
 * Review API service
 * Handles all product review-related API calls
 */
class ReviewApiService {
  /**
   * Add a new review for a product
   */
  async addReview(data: ReviewRequest): Promise<ReviewResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<ReviewResponse>>(
        '/v1/reviews',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Review added successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add review');
      return null;
    }
  }

  /**
   * Update an existing review
   */
  async updateReview(
    reviewId: number,
    data: ReviewRequest
  ): Promise<ReviewResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<ReviewResponse>>(
        `/v1/reviews/${reviewId}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Review updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update review');
      return null;
    }
  }

  /**
   * Delete a review
   */
  async deleteReview(reviewId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/reviews/${reviewId}`
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Review deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete review');
      return false;
    }
  }

  /**
   * Get all reviews for a product with pagination
   */
  async getProductReviews(
    productId: number,
    params?: {
      includeUnapproved?: boolean;
      page?: number;
      size?: number;
    }
  ): Promise<PaginatedResponse<ReviewResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ReviewResponse>>>(
        `/v1/reviews/product/${productId}`,
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch reviews');
      return null;
    }
  }

  /**
   * Get review statistics for a product
   */
  async getReviewStats(productId: number): Promise<any | null> {
    try {
      const response = await apiClient.get<ApiResponse<any>>(
        `/v1/reviews/product/${productId}/stats`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch review statistics');
      return null;
    }
  }
}

export const reviewApi = new ReviewApiService();
export default reviewApi;
