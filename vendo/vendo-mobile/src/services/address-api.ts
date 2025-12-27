import { apiClient, handleApiError, showSuccessAlert } from '@/lib/api';

export interface AddressResponse {
  id: number;
  addressType: 'SHIPPING' | 'BILLING' | 'BOTH';
  contactName: string;
  phone?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AddAddressRequest {
  addressType?: 'SHIPPING' | 'BILLING' | 'BOTH';
  contactName?: string;
  phone?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault?: boolean;
}

class AddressApiService {
  async getAddresses(): Promise<AddressResponse[]> {
    try {
      const response = await apiClient.get<{ success: boolean; data: AddressResponse[] }>(
        '/v1/addresses'
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return [];
    } catch (error) {
      handleApiError(error, 'Failed to fetch addresses');
      return [];
    }
  }

  async addAddress(data: AddAddressRequest): Promise<AddressResponse | null> {
    try {
      const response = await apiClient.post<{ success: boolean; data: AddressResponse; message?: string }>(
        '/v1/addresses',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessAlert(response.data.message || 'Address added successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add address');
      return null;
    }
  }
}

export const addressApi = new AddressApiService();
export default addressApi;

