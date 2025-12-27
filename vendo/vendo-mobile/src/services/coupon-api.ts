import { apiClient, handleApiError } from '@/lib/api';

export interface CouponResponse {
  id: number;
  code: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED';
  discountValue: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  isValid: boolean;
  validationMessage?: string;
  calculatedDiscount?: number;
}

class CouponApiService {
  async validateCoupon(
    code: string,
    orderAmount?: number
  ): Promise<CouponResponse | null> {
    try {
      const params: Record<string, string | number> = { code };
      if (orderAmount !== undefined) {
        params.orderAmount = orderAmount;
      }

      const response = await apiClient.get<{ success: boolean; data: CouponResponse }>(
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
}

export const couponApi = new CouponApiService();
export default couponApi;

