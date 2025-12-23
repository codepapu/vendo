import { apiClient, handleApiError } from '@/lib/api';
import type {
  OrderResponse,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

/**
 * Order API service
 * Handles all order-related API calls
 */
class OrderApiService {
  /**
   * Get all orders for current user
   */
  async getOrders(
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<OrderResponse>> {
    try {
      const params: Record<string, string | number> = {
        page,
        size,
      };
      
      if (status) {
        params.status = status;
      }

      const response = await apiClient.get<ApiResponse<PaginatedResponse<OrderResponse>>>(
        '/v1/orders',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      // Return empty paginated response if no data
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size,
        number: page,
        first: true,
        last: true,
        numberOfElements: 0,
        empty: true,
      };
    } catch (error) {
      handleApiError(error, 'Failed to fetch orders');
      return {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size,
        number: page,
        first: true,
        last: true,
        numberOfElements: 0,
        empty: true,
      };
    }
  }

  /**
   * Get order by ID
   */
  async getOrderById(orderId: number): Promise<OrderResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<OrderResponse>>(
        `/v1/orders/${orderId}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch order');
      return null;
    }
  }

  /**
   * Get order by code
   */
  async getOrderByCode(code: string): Promise<OrderResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<OrderResponse>>(
        `/v1/orders/code/${code}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch order');
      return null;
    }
  }

  /**
   * Create a new order
   */
  async createOrder(orderData: {
    items: Array<{ productId: number; quantity: number; variationId?: number }>;
    shippingAddress: {
      name: string;
      phone: string;
      email: string;
      address: string;
      city: string;
      state: string;
      zipCode: string;
      country: string;
    };
    billingAddress: {
      name: string;
      phone: string;
      email: string;
      address: string;
      city: string;
      state: string;
      zipCode: string;
      country: string;
    };
    shippingMethod?: string;
    couponCode?: string;
    notes?: string;
  }): Promise<OrderResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<OrderResponse>>(
        '/v1/orders',
        orderData
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to create order');
      return null;
    }
  }
}

export const orderApi = new OrderApiService();
export default orderApi;
