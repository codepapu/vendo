import { apiClient } from '@/lib/api';

export interface ReturnRequest {
  orderId: number;
  reason: string;
  items: Array<{
    orderItemId: number;
    quantity: number;
    reason?: string;
    condition?: string;
  }>;
}

export interface ReturnResponse {
  id: number;
  code: string;
  orderId: number;
  userId: number;
  reason: string;
  status: string;
  refundAmount: number;
  refundStatus: string;
  createdAt: string;
  updatedAt: string;
  items: Array<{
    id: number;
    orderItemId: number;
    productId: number;
    productName: string;
    quantity: number;
    price: number;
    refundAmount: number;
    reason?: string;
    condition?: string;
  }>;
  history?: ReturnHistoryEntry[];
}

export interface ReturnHistoryEntry {
  id: number;
  status: string;
  description?: string;
  createdAt: string;
}

class ReturnApiService {
  async createReturnRequest(data: ReturnRequest): Promise<ReturnResponse> {
    const response = await apiClient.post<ReturnResponse>('/v1/returns', data);
    return response.data;
  }

  async getReturnById(id: number): Promise<ReturnResponse> {
    const response = await apiClient.get<ReturnResponse>(`/v1/returns/${id}`);
    return response.data;
  }

  async getReturnByCode(code: string): Promise<ReturnResponse> {
    const response = await apiClient.get<ReturnResponse>(`/v1/returns/code/${code}`);
    return response.data;
  }

  async getReturnsByOrderId(orderId: number): Promise<ReturnResponse[]> {
    const response = await apiClient.get<ReturnResponse[]>(`/v1/returns/order/${orderId}`);
    return response.data;
  }

  async getReturnHistory(): Promise<ReturnResponse[]> {
    const response = await apiClient.get<ReturnResponse[]>('/v1/returns/history');
    return response.data;
  }

  async approveReturn(id: number, notes?: string): Promise<ReturnResponse> {
    const response = await apiClient.post<ReturnResponse>(
      `/v1/returns/${id}/approve`,
      null,
      { params: { notes } }
    );
    return response.data;
  }

  async rejectReturn(id: number, reason?: string): Promise<ReturnResponse> {
    const response = await apiClient.post<ReturnResponse>(
      `/v1/returns/${id}/reject`,
      null,
      { params: { reason } }
    );
    return response.data;
  }

  async processRefund(id: number): Promise<ReturnResponse> {
    const response = await apiClient.post<ReturnResponse>(`/v1/returns/${id}/refund`);
    return response.data;
  }

  async getReturnsByStatus(status?: string): Promise<ReturnResponse[]> {
    const response = await apiClient.get<ReturnResponse[]>(
      `/v1/returns/status/${status || 'PENDING'}`
    );
    return response.data;
  }
}

export const returnApi = new ReturnApiService();

