import { apiClient } from '@/lib/api';

export interface SearchRequest {
  query?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  brand?: string;
  minRating?: number;
  page?: number;
  size?: number;
}

export interface SearchResponse {
  products: Array<{
    id: number;
    name: string;
    slug: string;
    description: string;
    imageUrl: string;
    price: number;
    salePrice?: number;
    rating: number;
    reviewCount: number;
    category: string;
    brand: string;
  }>;
  totalResults: number;
  page: number;
  size: number;
  facets: Record<string, Array<{ value: string; count: number }>>;
  suggestions: string[];
}

class SearchApiService {
  async searchProducts(request: SearchRequest): Promise<SearchResponse> {
    const response = await apiClient.get<SearchResponse>('/v1/search/products', {
      params: request,
    });
    return response.data;
  }

  async getSuggestions(query: string, limit: number = 10): Promise<string[]> {
    const response = await apiClient.get<string[]>('/v1/search/suggestions', {
      params: { query, limit },
    });
    return response.data;
  }
}

export const searchApi = new SearchApiService();

