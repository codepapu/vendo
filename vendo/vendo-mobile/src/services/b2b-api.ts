import { apiClient, handleApiError } from '@/lib/api';

/**
 * B2B API Response Types
 */
export interface BusinessAccountResponse {
  id: number;
  userId: number;
  companyName: string;
  businessRegistrationNumber?: string;
  taxId?: string;
  businessType: 'CORPORATE' | 'SME' | 'ENTERPRISE';
  creditLimit: number;
  creditTermsDays: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
  availableCredit: number;
  totalOrders: number;
  totalSpent: number;
  createdAt: string;
  updatedAt: string;
}

export interface B2BPricing {
  productId: number;
  quantity: number;
  unitPrice: number;
  discount: number;
  discountPercentage?: number;
  totalPrice: number;
  priceTier?: string;
}

export interface PurchaseOrderResponse {
  id: number;
  poNumber: string;
  businessAccountId: number;
  orderId?: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
  requestedAt: string;
  approvedAt?: string;
  createdAt: string;
  order?: {
    id: number;
    orderNumber: string;
    totalAmount: number;
  };
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  orderId?: number;
  businessAccountId: number;
  issueDate: string;
  dueDate: string;
  status: 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  paidAmount: number;
  paidAt?: string;
  createdAt: string;
  order?: {
    id: number;
    orderNumber: string;
  };
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * B2B API Service
 */
class B2BApiService {
  /**
   * Get business account for current user
   */
  async getAccount(): Promise<BusinessAccountResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<BusinessAccountResponse>>(
        '/api/v1/b2b/accounts/me'
      );
      return response.data.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      handleApiError(error, 'Failed to get business account');
      return null;
    }
  }

  /**
   * Get B2B pricing for product
   */
  async getPricing(productId: number, quantity: number): Promise<B2BPricing | null> {
    try {
      const response = await apiClient.get<ApiResponse<B2BPricing>>(
        `/api/v1/b2b/pricing/${productId}`,
        { params: { quantity } }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to get B2B pricing');
      return null;
    }
  }

  /**
   * Get purchase orders
   */
  async getPurchaseOrders(
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<PurchaseOrderResponse> | null> {
    try {
      const params: any = { page, size };
      if (status) params.status = status;
      
      const response = await apiClient.get<ApiResponse<PaginatedResponse<PurchaseOrderResponse>>>(
        '/api/v1/b2b/purchase-orders',
        { params }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to get purchase orders');
      return null;
    }
  }

  /**
   * Get purchase order by ID
   */
  async getPurchaseOrder(poId: number): Promise<PurchaseOrderResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<PurchaseOrderResponse>>(
        `/api/v1/b2b/purchase-orders/${poId}`
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to get purchase order');
      return null;
    }
  }

  /**
   * Get invoices
   */
  async getInvoices(
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<InvoiceResponse> | null> {
    try {
      const params: any = { page, size };
      if (status) params.status = status;
      
      const response = await apiClient.get<ApiResponse<PaginatedResponse<InvoiceResponse>>>(
        '/api/v1/b2b/invoices',
        { params }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to get invoices');
      return null;
    }
  }

  /**
   * Get invoice by ID
   */
  async getInvoice(invoiceId: number): Promise<InvoiceResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<InvoiceResponse>>(
        `/api/v1/b2b/invoices/${invoiceId}`
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to get invoice');
      return null;
    }
  }
}

export const b2bApi = new B2BApiService();

