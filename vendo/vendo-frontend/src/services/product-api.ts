import { apiClient, handleApiError } from '@/lib/api';
import type {
  ProductResponse,
  ProductSearchRequest,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

/**
 * Product API service
 * Handles all product-related API calls
 */
class ProductApiService {
  /**
   * Get all products with pagination and filters
   */
  async getAllProducts(params?: {
    status?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: 'ASC' | 'DESC';
  }): Promise<PaginatedResponse<ProductResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ProductResponse>>>(
        '/v1/products',
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
   * Get product by ID
   */
  async getProductById(id: number): Promise<ProductResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<ProductResponse>>(
        `/v1/products/${id}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch product');
      return null;
    }
  }

  /**
   * Get product by SKU
   */
  async getProductBySku(sku: string): Promise<ProductResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<ProductResponse>>(
        `/v1/products/sku/${sku}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch product');
      return null;
    }
  }

  /**
   * Search products
   */
  async searchProducts(params: ProductSearchRequest): Promise<PaginatedResponse<ProductResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ProductResponse>>>(
        '/v1/products/search',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to search products');
      return null;
    }
  }

  /**
   * Get products by category
   */
  async getProductsByCategory(
    categoryId: number,
    params?: {
      status?: string;
      page?: number;
      size?: number;
    }
  ): Promise<PaginatedResponse<ProductResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ProductResponse>>>(
        `/v1/products/category/${categoryId}`,
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch products by category');
      return null;
    }
  }

  /**
   * Get products by brand
   */
  async getProductsByBrand(
    brandId: number,
    params?: {
      status?: string;
      page?: number;
      size?: number;
    }
  ): Promise<PaginatedResponse<ProductResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ProductResponse>>>(
        `/v1/products/brand/${brandId}`,
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch products by brand');
      return null;
    }
  }

  /**
   * Get products by price range
   */
  async getProductsByPriceRange(params?: {
    minPrice?: number;
    maxPrice?: number;
    status?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<ProductResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<ProductResponse>>>(
        '/v1/products/price-range',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch products by price range');
      return null;
    }
  }

  /**
   * Get featured products
   */
  async getFeaturedProducts(status?: string): Promise<ProductResponse[] | null> {
    try {
      const response = await apiClient.get<ApiResponse<ProductResponse[]>>(
        '/v1/products/featured',
        { params: { status } }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch featured products');
      return null;
    }
  }

  /**
   * Get product variations
   */
  async getProductVariations(productId: number): Promise<ProductResponse[] | null> {
    try {
      const response = await apiClient.get<ApiResponse<ProductResponse[]>>(
        `/v1/products/${productId}/variations`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch product variations');
      return null;
    }
  }

  /**
   * Create product (Admin/Vendor only)
   */
  async createProduct(data: any): Promise<ProductResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<ProductResponse>>(
        '/v1/products',
        data
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to create product');
      return null;
    }
  }

  /**
   * Update product (Admin/Vendor only)
   */
  async updateProduct(id: number, data: any): Promise<ProductResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<ProductResponse>>(
        `/v1/products/${id}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update product');
      return null;
    }
  }

  /**
   * Delete product (Admin/Vendor only)
   */
  async deleteProduct(id: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/products/${id}`
      );
      
      if (response.data.success) {
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete product');
      return false;
    }
  }
}

export const productApi = new ProductApiService();
export default productApi;
