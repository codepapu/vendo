import { apiClient, handleApiError } from '@/lib/api';

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
  createdAt: string;
}

class ShippingApiService {
  async getAvailableShippingMethods(): Promise<ShippingMethodResponse[]> {
    try {
      const response = await apiClient.get<{ success: boolean; data: ShippingMethodResponse[] }>(
        '/v1/shipping-methods'
      );
      return response.data.data || [];
    } catch (error) {
      handleApiError(error, 'Failed to fetch shipping methods');
      return [];
    }
  }
}

export const shippingApi = new ShippingApiService();
export default shippingApi;

