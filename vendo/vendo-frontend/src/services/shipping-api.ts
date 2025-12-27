import { apiClient } from '@/lib/api';

export interface CalculateShippingRequest {
  weight: number;
  length?: number;
  width?: number;
  height?: number;
  destination: {
    addressLine1?: string;
    addressLine2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
}

export interface CreateShipmentLabelRequest {
  orderId: number;
  shippingMethodId: number;
  carrier?: string;
  serviceType?: string;
}

export interface ShippingResponse {
  cost: number;
  estimatedDelivery: string;
  carrier?: string;
  serviceType?: string;
  trackingNumber?: string;
  labelUrl?: string;
  availableMethods: Array<{
    id: number;
    name: string;
    code: string;
    carrier: string;
    cost: number;
    estimatedDays: number;
    estimatedDelivery: string;
  }>;
}

class ShippingApiService {
  async calculateShipping(request: CalculateShippingRequest): Promise<ShippingResponse> {
    const response = await apiClient.post<ShippingResponse>('/api/v1/shipping/calculate', request);
    return response.data;
  }

  async getShippingMethods(): Promise<ShippingResponse['availableMethods']> {
    const response = await apiClient.get<ShippingResponse['availableMethods']>('/api/v1/shipping/methods');
    return response.data;
  }

  async trackShipment(trackingNumber: string, carrier?: string): Promise<any> {
    const response = await apiClient.get(`/api/v1/shipping/track/${trackingNumber}`, {
      params: { carrier },
    });
    return response.data;
  }

  async createShipmentLabel(request: CreateShipmentLabelRequest): Promise<ShippingResponse> {
    const response = await apiClient.post<ShippingResponse>('/api/v1/shipping/label', request);
    return response.data;
  }
}

export const shippingApi = new ShippingApiService();
