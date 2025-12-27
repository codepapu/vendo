import { apiClient, handleApiError, showSuccessAlert } from '@/lib/api';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  productImage?: string;
  quantity: number;
  price: number;
  total: number;
  variationId?: number;
}

export interface OrderResponse {
  id: number;
  code: string;
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDED';
  items: OrderItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
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
  paymentMethod?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

class OrderApiService {
  async getOrders(
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<OrderResponse>> {
    try {
      const params: Record<string, string | number> = { page, size };
      if (status) {
        params.status = status;
      }

      const response = await apiClient.get<{ success: boolean; data: PaginatedResponse<OrderResponse> }>(
        '/v1/orders',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
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

  async getOrderById(orderId: number): Promise<OrderResponse | null> {
    try {
      const response = await apiClient.get<{ success: boolean; data: OrderResponse }>(
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
      const response = await apiClient.post<{ success: boolean; data: OrderResponse }>(
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

