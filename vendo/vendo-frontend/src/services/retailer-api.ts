import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  Retailer,
  RetailerProduct,
  RetailerDashboard,
  RetailerOrder,
  RetailerRegistrationRequest,
  ProductListingRequest,
  UpdateProductListingRequest,
  RetailerAnalytics,
  ApiResponse,
  PaginatedResponse,
} from '@/types';
import type { RetailerResponse, RetailerDashboardResponse, RetailerAnalyticsResponse } from '@/types/retailer';

/**
 * Retailer API service
 * Handles all retailer-related API calls
 */
class RetailerApiService {
  /**
   * Register a new retailer
   */
  async registerRetailer(data: RetailerRegistrationRequest): Promise<Retailer | null> {
    try {
      const response = await apiClient.post<ApiResponse<Retailer>>(
        '/v1/retailers/register',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Retailer registration submitted successfully. Waiting for approval.');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to register retailer');
      return null;
    }
  }

  /**
   * Get retailer dashboard data
   */
  async getDashboard(): Promise<RetailerDashboard | null> {
    try {
      const response = await apiClient.get<ApiResponse<RetailerDashboard>>(
        '/v1/retailers/dashboard'
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch dashboard data');
      return null;
    }
  }

  /**
   * Get retailer by ID
   */
  async getRetailerById(id: number): Promise<Retailer | null> {
    try {
      const response = await apiClient.get<ApiResponse<Retailer>>(
        `/v1/retailers/${id}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch retailer');
      return null;
    }
  }

  /**
   * Get retailer by user ID
   */
  async getRetailerByUserId(userId: number): Promise<Retailer | null> {
    try {
      const response = await apiClient.get<ApiResponse<Retailer>>(
        `/v1/retailers/user/${userId}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch retailer');
      return null;
    }
  }

  /**
   * Update retailer information
   */
  async updateRetailer(id: number, data: Partial<RetailerRegistrationRequest>): Promise<Retailer | null> {
    try {
      const response = await apiClient.put<ApiResponse<Retailer>>(
        `/v1/retailers/${id}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Retailer information updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update retailer');
      return null;
    }
  }

  /**
   * Add product to retailer catalog
   */
  async addProduct(retailerId: number, data: ProductListingRequest): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.post<ApiResponse<RetailerProduct>>(
        `/v1/retailers/${retailerId}/products`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Product added successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to add product');
      return null;
    }
  }

  /**
   * Update product listing
   */
  async updateProduct(
    retailerId: number,
    productRetailerId: number,
    data: UpdateProductListingRequest
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.put<ApiResponse<RetailerProduct>>(
        `/v1/retailers/${retailerId}/products/${productRetailerId}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Product updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update product');
      return null;
    }
  }

  /**
   * Update product price
   */
  async updateProductPrice(
    retailerId: number,
    productRetailerId: number,
    newPrice: number
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.put<ApiResponse<RetailerProduct>>(
        `/v1/retailers/${retailerId}/products/${productRetailerId}/price`,
        null,
        { params: { newPrice } }
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Product price updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update product price');
      return null;
    }
  }

  /**
   * Update product stock
   */
  async updateStock(
    retailerId: number,
    productRetailerId: number,
    newStockQuantity: number
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.put<ApiResponse<RetailerProduct>>(
        `/v1/retailers/${retailerId}/products/${productRetailerId}/stock`,
        null,
        { params: { newStockQuantity } }
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Stock updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update stock');
      return null;
    }
  }

  /**
   * Get retailer products with pagination and filters
   */
  async getProducts(
    retailerId: number,
    params?: {
      isActive?: boolean;
      searchTerm?: string;
      page?: number;
      size?: number;
      sortBy?: string;
      sortDir?: 'ASC' | 'DESC';
    }
  ): Promise<PaginatedResponse<RetailerProduct> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<RetailerProduct>>>(
        `/v1/retailers/${retailerId}/products`,
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch products');
      return null;
    }
  }

  /**
   * Deactivate product
   */
  async deactivateProduct(
    retailerId: number,
    productRetailerId: number
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.put<ApiResponse<RetailerProduct>>(
        `/v1/retailers/${retailerId}/products/${productRetailerId}/deactivate`
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Product deactivated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to deactivate product');
      return null;
    }
  }

  /**
   * Activate product
   */
  async activateProduct(
    retailerId: number,
    productRetailerId: number
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.put<ApiResponse<RetailerProduct>>(
        `/v1/retailers/${retailerId}/products/${productRetailerId}/activate`
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Product activated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to activate product');
      return null;
    }
  }

  /**
   * Get retailer orders with pagination and filters
   */
  async getOrders(
    retailerId: number,
    params?: {
      status?: string;
      page?: number;
      size?: number;
      sortBy?: string;
      sortDir?: 'ASC' | 'DESC';
    }
  ): Promise<PaginatedResponse<RetailerOrder> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<RetailerOrder>>>(
        `/v1/retailers/${retailerId}/orders`,
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch orders');
      return null;
    }
  }

  /**
   * Get retailer analytics
   */
  async getAnalytics(
    retailerId: number,
    params?: {
      startDate?: string;
      endDate?: string;
    }
  ): Promise<RetailerAnalytics | null> {
    try {
      const response = await apiClient.get<ApiResponse<RetailerAnalytics>>(
        `/v1/retailers/${retailerId}/analytics`,
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch analytics');
      return null;
    }
  }

  /**
   * Upload product image
   */
  async uploadProductImage(file: File): Promise<string | null> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await apiClient.post<ApiResponse<{ url: string }>>(
        '/v1/media/upload',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data.url;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to upload image');
      return null;
    }
  }

  /**
   * Upload store logo
   */
  async uploadStoreLogo(file: File): Promise<string | null> {
    return this.uploadProductImage(file);
  }

  /**
   * Upload store banner
   */
  async uploadStoreBanner(file: File): Promise<string | null> {
    return this.uploadProductImage(file);
  }

  /**
   * Check if store slug is available
   */
  async checkSlugAvailability(slug: string): Promise<boolean> {
    try {
      const response = await apiClient.get<ApiResponse<{ available: boolean }>>(
        `/v1/retailers/slug/${slug}/check`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data.available;
      }
      
      return false;
    } catch (error) {
      // If 404, slug is available
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } };
        if (axiosError.response?.status === 404) {
          return true;
        }
      }
      return false;
    }
  }
}

export const retailerApi = new RetailerApiService();
export default retailerApi;

