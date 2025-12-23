import { apiClient, handleApiError } from '@/lib/api';
import type { ApiResponse } from '@/types';

/**
 * Shipping method response type
 */
export interface ShippingMethodResponse {
  id: number;
  name: string;
  code: string;
  description?: string;
  carrier?: string;
  baseCost: number;
  costPerKg?: number;
  estimatedDays?: number;
  isActive: boolean;
  createdAt: string; // ISO date string
}

/**
 * Shipping API service
 * Handles shipping method-related API calls
 */
class ShippingApiService {
  /**
   * Get available shipping methods
   * Note: This endpoint may need to be created in the backend
   * For now, we'll use a placeholder endpoint
   */
  async getAvailableShippingMethods(): Promise<ShippingMethodResponse[]> {
    try {
      // TODO: Update this endpoint when shipping methods API is available
      // For now, return default shipping methods
      const defaultMethods: ShippingMethodResponse[] = [
        {
          id: 1,
          name: 'Standard Shipping',
          code: 'STANDARD',
          description: 'Standard shipping (5-7 business days)',
          baseCost: 5.99,
          estimatedDays: 7,
          isActive: true,
          createdAt: new Date().toISOString(),
        },
        {
          id: 2,
          name: 'Express Shipping',
          code: 'EXPRESS',
          description: 'Express shipping (2-3 business days)',
          baseCost: 12.99,
          estimatedDays: 3,
          isActive: true,
          createdAt: new Date().toISOString(),
        },
        {
          id: 3,
          name: 'Overnight Shipping',
          code: 'OVERNIGHT',
          description: 'Overnight shipping (1 business day)',
          baseCost: 24.99,
          estimatedDays: 1,
          isActive: true,
          createdAt: new Date().toISOString(),
        },
      ];
      
      return defaultMethods;
    } catch (error) {
      handleApiError(error, 'Failed to fetch shipping methods');
      return [];
    }
  }

  /**
   * Calculate shipping cost
   * Note: This would typically calculate based on weight, distance, etc.
   */
  async calculateShippingCost(
    shippingMethodId: number,
    weight?: number
  ): Promise<number> {
    try {
      const methods = await this.getAvailableShippingMethods();
      const method = methods.find((m) => m.id === shippingMethodId);
      
      if (!method) {
        return 0;
      }
      
      let cost = method.baseCost;
      if (method.costPerKg && weight) {
        cost += method.costPerKg * weight;
      }
      
      return cost;
    } catch (error) {
      handleApiError(error, 'Failed to calculate shipping cost');
      return 0;
    }
  }
}

export const shippingApi = new ShippingApiService();
export default shippingApi;
