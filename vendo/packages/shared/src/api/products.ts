import { ApiResponse, Product } from '../types';
import { ApiClient } from './client';

export interface ProductFilters {
  categoryId?: number;
  vendorId?: number;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export class ProductsApi {
  constructor(private client: ApiClient) {}

  async getProducts(filters?: ProductFilters): Promise<ApiResponse<Product[]>> {
    const response = await this.client.instance.get<ApiResponse<Product[]>>('/v1/products', {
      params: filters,
    });
    return response.data;
  }

  async getProduct(id: number): Promise<ApiResponse<Product>> {
    const response = await this.client.instance.get<ApiResponse<Product>>(`/v1/products/${id}`);
    return response.data;
  }

  async getProductsByCategory(categoryId: number): Promise<ApiResponse<Product[]>> {
    const response = await this.client.instance.get<ApiResponse<Product[]>>('/v1/products', {
      params: { categoryId },
    });
    return response.data;
  }
}
