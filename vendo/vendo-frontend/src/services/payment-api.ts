import { apiClient, handleApiError } from '@/lib/api';
import { PaymentResponse, ProcessPaymentRequest, RefundPaymentRequest } from '@/types';

/**
 * Payment API Service
 * Handles all payment-related API calls
 */
export const paymentApi = {
  /**
   * Process a payment
   */
  async processPayment(request: ProcessPaymentRequest): Promise<PaymentResponse> {
    try {
      const response = await apiClient.post<{ data: PaymentResponse; message: string }>(
        '/v1/payments/process',
        request
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to process payment');
      throw error;
    }
  },

  /**
   * Get payment by ID
   */
  async getPaymentById(id: number): Promise<PaymentResponse> {
    try {
      const response = await apiClient.get<{ data: PaymentResponse }>(`/v1/payments/${id}`);
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch payment');
      throw error;
    }
  },

  /**
   * Get payment by charge ID
   */
  async getPaymentByChargeId(chargeId: string): Promise<PaymentResponse> {
    try {
      const response = await apiClient.get<{ data: PaymentResponse }>(
        `/v1/payments/charge/${chargeId}`
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch payment');
      throw error;
    }
  },

  /**
   * Get payments by order ID
   */
  async getPaymentsByOrderId(orderId: number): Promise<PaymentResponse[]> {
    try {
      const response = await apiClient.get<{ data: PaymentResponse[] }>(
        `/v1/payments/order/${orderId}`
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch payments');
      throw error;
    }
  },

  /**
   * Process a refund
   */
  async processRefund(request: RefundPaymentRequest): Promise<PaymentResponse> {
    try {
      const response = await apiClient.post<{ data: PaymentResponse; message: string }>(
        '/v1/payments/refund',
        request
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to process refund');
      throw error;
    }
  },

  /**
   * Get payment history for current user
   */
  async getPaymentHistory(): Promise<PaymentResponse[]> {
    try {
      const response = await apiClient.get<{ data: PaymentResponse[] }>('/v1/payments/history');
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch payment history');
      throw error;
    }
  },

  /**
   * Get all payments with filters
   */
  async getAllPayments(params?: {
    userId?: number;
    orderId?: number;
    status?: string;
    page?: number;
    size?: number;
  }): Promise<{ data: PaymentResponse[]; total: number; page: number; size: number }> {
    try {
      const response = await apiClient.get<{
        data: { content: PaymentResponse[]; totalElements: number; number: number; size: number };
      }>('/v1/payments', { params });
      return {
        data: response.data.data.content,
        total: response.data.data.totalElements,
        page: response.data.data.number,
        size: response.data.data.size,
      };
    } catch (error) {
      handleApiError(error, 'Failed to fetch payments');
      throw error;
    }
  },

  /**
   * Update payment status (admin only)
   */
  async updatePaymentStatus(id: number, status: string): Promise<PaymentResponse> {
    try {
      const response = await apiClient.put<{ data: PaymentResponse; message: string }>(
        `/v1/payments/${id}/status`,
        null,
        { params: { status } }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to update payment status');
      throw error;
    }
  },

  /**
   * Handle webhook (internal use)
   */
  async handleWebhook(
    gateway: string,
    payload: string,
    signature?: string
  ): Promise<PaymentResponse> {
    try {
      const headers: Record<string, string> = {};
      if (signature) {
        headers['X-Signature'] = signature;
      }
      const response = await apiClient.post<{ data: PaymentResponse; message: string }>(
        `/v1/payments/webhook/${gateway}`,
        payload,
        { headers }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to process webhook');
      throw error;
    }
  },
};


export default paymentApi;
