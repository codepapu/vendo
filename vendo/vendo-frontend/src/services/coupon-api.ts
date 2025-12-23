import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type { ApiResponse, CouponResponse } from '@/types';

/**
 * Apply coupon request type
 */
export interface ApplyCouponRequest {
  code: string;
  orderAmount: number;
  productIds?: number[];
  categoryIds?: number[];
}

/**
 * Coupon API service
 * Handles all coupon-related API calls
 */
class CouponApiService {
  /**
   * Validate a coupon code
   */
  async validateCoupon(
    code: string,
    orderAmount?: number
  ): Promise<CouponResponse | null> {
    try {
      const params: Record<string, string | number> = { code };
      if (orderAmount !== undefined) {
        params.orderAmount = orderAmount;
      }

      const response = await apiClient.get<ApiResponse<CouponResponse>>(
        '/v1/coupons/validate',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to validate coupon');
      return null;
    }
  }

  /**
   * Apply a coupon to an order
   */
  async applyCoupon(data: ApplyCouponRequest): Promise<CouponResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<CouponResponse>>(
        '/v1/coupons/apply',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Coupon applied successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to apply coupon');
      return null;
    }
  }

  /**
   * Get available coupons
   */
  async getAvailableCoupons(): Promise<CouponResponse[]> {
    try {
      const response = await apiClient.get<ApiResponse<CouponResponse[]>>(
        '/v1/coupons/available'
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return [];
    } catch (error) {
      handleApiError(error, 'Failed to fetch available coupons');
      return [];
    }
  }
}

export const couponApi = new CouponApiService();
export default couponApi;
