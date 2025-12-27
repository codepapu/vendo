import { apiClient } from '@/lib/api';
import {
  BusinessAccount,
  BusinessAccountRequest,
  BusinessAccountResponse,
  B2BPricing,
  PurchaseOrder,
  PurchaseOrderRequest,
  PurchaseOrderResponse,
  Invoice,
  InvoiceResponse,
} from '@/types/b2b';

class B2BApiService {
  /**
   * Create business account
   */
  async createAccount(data: BusinessAccountRequest): Promise<BusinessAccountResponse> {
    try {
      const response = await apiClient.post<BusinessAccountResponse>('/api/v1/b2b/accounts', data);
      return response.data;
    } catch (error: any) {
      console.error('Failed to create business account:', error);
      throw error;
    }
  }

  /**
   * Get business account
   */
  async getAccount(accountId?: number): Promise<BusinessAccountResponse | null> {
    try {
      const endpoint = accountId 
        ? `/api/v1/b2b/accounts/${accountId}`
        : '/api/v1/b2b/accounts/me';
      const response = await apiClient.get<BusinessAccountResponse>(endpoint);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      console.error('Failed to get business account:', error);
      throw error;
    }
  }

  /**
   * Get B2B pricing for product
   */
  async getPricing(productId: number, quantity: number): Promise<B2BPricing> {
    try {
      const response = await apiClient.get<B2BPricing>(
        `/api/v1/b2b/pricing/${productId}`,
        { params: { quantity } }
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to get B2B pricing:', error);
      throw error;
    }
  }

  /**
   * Create purchase order
   */
  async createPurchaseOrder(data: PurchaseOrderRequest): Promise<PurchaseOrderResponse> {
    try {
      const response = await apiClient.post<PurchaseOrderResponse>(
        '/api/v1/b2b/purchase-orders',
        data
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to create purchase order:', error);
      throw error;
    }
  }

  /**
   * Get purchase orders
   */
  async getPurchaseOrders(
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<{ content: PurchaseOrderResponse[]; totalElements: number }> {
    try {
      const params: any = { page, size };
      if (status) params.status = status;
      
      const response = await apiClient.get<{ content: PurchaseOrderResponse[]; totalElements: number }>(
        '/api/v1/b2b/purchase-orders',
        { params }
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to get purchase orders:', error);
      throw error;
    }
  }

  /**
   * Get purchase order by ID
   */
  async getPurchaseOrder(poId: number): Promise<PurchaseOrderResponse> {
    try {
      const response = await apiClient.get<PurchaseOrderResponse>(
        `/api/v1/b2b/purchase-orders/${poId}`
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to get purchase order:', error);
      throw error;
    }
  }

  /**
   * Get invoices
   */
  async getInvoices(
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<{ content: InvoiceResponse[]; totalElements: number }> {
    try {
      const params: any = { page, size };
      if (status) params.status = status;
      
      const response = await apiClient.get<{ content: InvoiceResponse[]; totalElements: number }>(
        '/api/v1/b2b/invoices',
        { params }
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to get invoices:', error);
      throw error;
    }
  }

  /**
   * Get invoice by ID
   */
  async getInvoice(invoiceId: number): Promise<InvoiceResponse> {
    try {
      const response = await apiClient.get<InvoiceResponse>(
        `/api/v1/b2b/invoices/${invoiceId}`
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to get invoice:', error);
      throw error;
    }
  }

  /**
   * Mark invoice as paid
   */
  async markInvoicePaid(invoiceId: number, paymentMethod: string): Promise<InvoiceResponse> {
    try {
      const response = await apiClient.post<InvoiceResponse>(
        `/api/v1/b2b/invoices/${invoiceId}/mark-paid`,
        { paymentMethod }
      );
      return response.data;
    } catch (error: any) {
      console.error('Failed to mark invoice as paid:', error);
      throw error;
    }
  }
}

export const b2bApi = new B2BApiService();

