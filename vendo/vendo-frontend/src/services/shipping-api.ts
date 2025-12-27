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
  calculatedCost?: number; // Calculated cost based on weight
  totalWeight?: number; // Total weight used for calculation
}

/**
 * Shipping estimate request
 */
export interface ShippingEstimateRequest {
  address?: {
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  totalWeight?: number;
  addressWeights?: Array<{
    address: {
      city: string;
      state: string;
      postalCode: string;
      country: string;
    };
    weight: number;
  }>;
}

/**
 * Shipping estimate response
 */
export interface ShippingEstimateResponse {
  availableMethods: ShippingMethodResponse[];
  totalWeight: number;
  shippingAddress?: {
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
  addressEstimates?: Array<{
    address: {
      city: string;
      state: string;
      postalCode: string;
      country: string;
    };
    weight: number;
    availableMethods: ShippingMethodResponse[];
  }>;
}

/**
 * Select shipping method request
 */
export interface SelectShippingMethodRequest {
  shippingMethodId: number;
  addressShippingMethods?: Array<{
    addressId: number;
    shippingMethodId: number;
  }>;
}

/**
 * Shipping API service
 * Handles shipping method-related API calls
 */
class ShippingApiService {
  /**
   * Get all available shipping methods
   * GET /api/v1/shipping-methods
   */
  async getAvailableShippingMethods(): Promise<ShippingMethodResponse[]> {
    try {
      const response = await apiClient.get<ApiResponse<ShippingMethodResponse[]>>(
        '/api/v1/shipping-methods'
      );
      return response.data.data || [];
    } catch (error) {
      handleApiError(error, 'Failed to fetch shipping methods');
      return [];
    }
  }

  /**
   * Get available shipping methods based on address and weight
   * GET /api/v1/shipping-methods/available?city={city}&state={state}&postalCode={postalCode}&country={country}&weight={weight}
   */
  async getAvailableShippingMethodsForAddress(
    address: {
      city: string;
      state: string;
      postalCode: string;
      country: string;
    },
    weight: number
  ): Promise<ShippingMethodResponse[]> {
    try {
      const params = new URLSearchParams({
        city: address.city,
        state: address.state,
        postalCode: address.postalCode,
        country: address.country,
        weight: weight.toString(),
      });

      const response = await apiClient.get<ApiResponse<ShippingMethodResponse[]>>(
        `/api/v1/shipping-methods/available?${params.toString()}`
      );
      return response.data.data || [];
    } catch (error) {
      handleApiError(error, 'Failed to fetch shipping methods for address');
      return [];
    }
  }

  /**
   * Calculate shipping cost for a specific method and weight
   * GET /api/v1/shipping-methods/{id}/calculate?weight={weight}
   */
  async calculateShippingCost(
    shippingMethodId: number,
    weight: number
  ): Promise<number> {
    try {
      const response = await apiClient.get<ApiResponse<number>>(
        `/api/v1/shipping-methods/${shippingMethodId}/calculate?weight=${weight}`
      );
      return response.data.data || 0;
    } catch (error) {
      handleApiError(error, 'Failed to calculate shipping cost');
      return 0;
    }
  }

  /**
   * Calculate shipping cost by method code
   * GET /api/v1/shipping-methods/calculate?code={code}&weight={weight}
   */
  async calculateShippingCostByCode(
    code: string,
    weight: number
  ): Promise<number> {
    try {
      const response = await apiClient.get<ApiResponse<number>>(
        `/api/v1/shipping-methods/calculate?code=${code}&weight=${weight}`
      );
      return response.data.data || 0;
    } catch (error) {
      handleApiError(error, 'Failed to calculate shipping cost');
      return 0;
    }
  }

  /**
   * Select shipping method
   * POST /api/v1/shipping-methods/select
   */
  async selectShippingMethod(
    request: SelectShippingMethodRequest,
    weight?: number
  ): Promise<ShippingMethodResponse> {
    try {
      const params = weight ? `?weight=${weight}` : '';
      const response = await apiClient.post<ApiResponse<ShippingMethodResponse>>(
        `/api/v1/shipping-methods/select${params}`,
        request
      );
      return response.data.data!;
    } catch (error) {
      handleApiError(error, 'Failed to select shipping method');
      throw error;
    }
  }

  /**
   * Get shipping estimates
   * POST /api/v1/shipping-methods/estimates
   */
  async getShippingEstimates(
    request: ShippingEstimateRequest
  ): Promise<ShippingEstimateResponse> {
    try {
      const response = await apiClient.post<ApiResponse<ShippingEstimateResponse>>(
        '/api/v1/shipping-methods/estimates',
        request
      );
      return response.data.data!;
    } catch (error) {
      handleApiError(error, 'Failed to get shipping estimates');
      throw error;
    }
  }

  /**
   * Get shipping estimates (simple GET version)
   * GET /api/v1/shipping-methods/estimates?city={city}&state={state}&postalCode={postalCode}&country={country}&weight={weight}
   */
  async getShippingEstimatesSimple(
    address: {
      city: string;
      state: string;
      postalCode: string;
      country: string;
    },
    weight: number
  ): Promise<ShippingEstimateResponse> {
    try {
      const params = new URLSearchParams({
        city: address.city,
        state: address.state,
        postalCode: address.postalCode,
        country: address.country,
        weight: weight.toString(),
      });

      const response = await apiClient.get<ApiResponse<ShippingEstimateResponse>>(
        `/api/v1/shipping-methods/estimates?${params.toString()}`
      );
      return response.data.data!;
    } catch (error) {
      handleApiError(error, 'Failed to get shipping estimates');
      throw error;
    }
  }
}

export const shippingApi = new ShippingApiService();
export default shippingApi;
