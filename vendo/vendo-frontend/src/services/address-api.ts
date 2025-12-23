import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  AddressResponse,
  ApiResponse,
  Address,
} from '@/types';

/**
 * Address request types
 */
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

export interface UpdateAddressRequest {
  addressType?: 'SHIPPING' | 'BILLING' | 'BOTH';
  contactName?: string;
  phone?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  isDefault?: boolean;
}

/**
 * Address API service
 * Handles all address-related API calls
 */
class AddressApiService {
  /**
   * Get all addresses for current user
   */
  async getAddresses(): Promise<AddressResponse[]> {
    try {
      const response = await apiClient.get<ApiResponse<AddressResponse[]>>(
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

  /**
   * Add a new address
   */
  async addAddress(data: AddAddressRequest): Promise<AddressResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<AddressResponse>>(
        '/v1/addresses',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Address added successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add address');
      return null;
    }
  }

  /**
   * Update an existing address
   */
  async updateAddress(
    addressId: number,
    data: UpdateAddressRequest
  ): Promise<AddressResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<AddressResponse>>(
        `/v1/addresses/${addressId}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Address updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update address');
      return null;
    }
  }

  /**
   * Delete an address
   */
  async deleteAddress(addressId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/addresses/${addressId}`
      );
      
      if (response.data.success) {
        showSuccessToast(response.data.message || 'Address deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete address');
      return false;
    }
  }

  /**
   * Set an address as default
   */
  async setDefaultAddress(addressId: number): Promise<AddressResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<AddressResponse>>(
        `/v1/addresses/${addressId}/default`
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast(response.data.message || 'Default address updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to set default address');
      return null;
    }
  }
}

export const addressApi = new AddressApiService();
export default addressApi;
