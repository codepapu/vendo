import { apiClient, handleApiError } from '@/lib/api';

/**
 * Retailer API Response Types
 */
export interface Retailer {
  id: number;
  userId: number;
  storeName: string;
  storeSlug: string;
  description?: string;
  logoUrl?: string;
  bannerUrl?: string;
  commissionRate: number;
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED';
  rating?: number;
  totalSales?: number;
  totalOrders?: number;
  verified?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RetailerDashboard {
  retailerId: number;
  storeName: string;
  status: string;
  totalSales: number;
  totalOrders: number;
  activeProducts: number;
  lowStockProducts: number;
  totalCommissions: number;
  pendingCommissions: number;
  thisMonthSales: number;
  thisMonthOrders: number;
  recentOrders: RecentOrder[];
  topProducts: TopProduct[];
}

export interface RecentOrder {
  orderId: number;
  orderNumber: string;
  totalAmount: number;
  status: string;
  createdAt: string;
}

export interface TopProduct {
  productId: number;
  productName: string;
  quantitySold: number;
  totalRevenue: number;
}

export interface RetailerProduct {
  id: number;
  productId: number;
  retailerId: number;
  price: number;
  compareAtPrice?: number;
  costPrice?: number;
  stockQuantity: number;
  lowStockThreshold: number;
  isActive: boolean;
  isLowStock: boolean;
  product?: {
    id: number;
    name: string;
    sku: string;
    images?: string[];
  };
  createdAt: string;
  updatedAt: string;
}

export interface RetailerOrder {
  id: number;
  code: string;
  orderType: 'B2C' | 'B2B' | 'RETAILER';
  status: string;
  paymentStatus?: string;
  amount: number;
  items: RetailerOrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface RetailerOrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  total: number;
}

export interface RetailerAnalytics {
  salesOverTime: SalesDataPoint[];
  topProducts: TopProduct[];
  revenueByCategory: CategoryRevenue[];
  customerDemographics: CustomerDemographic[];
  commissionBreakdown: CommissionBreakdown;
  payoutHistory: Payout[];
}

export interface SalesDataPoint {
  date: string;
  sales: number;
  orders: number;
  revenue: number;
}

export interface CategoryRevenue {
  categoryId: number;
  categoryName: string;
  revenue: number;
  orderCount: number;
}

export interface CustomerDemographic {
  segment: string;
  count: number;
  totalSpent: number;
}

export interface CommissionBreakdown {
  totalCommissions: number;
  pendingCommissions: number;
  paidCommissions: number;
  averageCommissionRate: number;
  byPeriod: {
    period: string;
    amount: number;
  }[];
}

export interface Payout {
  id: number;
  retailerId: number;
  amount: number;
  status: 'PENDING' | 'PROCESSED' | 'FAILED';
  processedAt?: string;
  createdAt: string;
  description?: string;
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

export interface ProductListingRequest {
  productId: number;
  price: number;
  compareAtPrice?: number;
  costPrice?: number;
  stockQuantity?: number;
  lowStockThreshold?: number;
  isActive?: boolean;
}

/**
 * Retailer API Service
 */
class RetailerApiService {
  /**
   * Get retailer dashboard
   */
  async getDashboard(): Promise<RetailerDashboard | null> {
    try {
      const response = await apiClient.get<ApiResponse<RetailerDashboard>>(
        '/api/v1/retailers/dashboard'
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch dashboard data');
      return null;
    }
  }

  /**
   * Get retailer by user ID
   */
  async getRetailerByUserId(userId: number): Promise<Retailer | null> {
    try {
      const response = await apiClient.get<ApiResponse<Retailer>>(
        `/api/v1/retailers/user/${userId}`
      );
      return response.data.data;
    } catch (error) {
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } };
        if (axiosError.response?.status === 404) {
          return null;
        }
      }
      handleApiError(error, 'Failed to fetch retailer');
      return null;
    }
  }

  /**
   * Get retailer products
   */
  async getProducts(
    retailerId: number,
    params?: {
      isActive?: boolean;
      searchTerm?: string;
      page?: number;
      size?: number;
    }
  ): Promise<PaginatedResponse<RetailerProduct> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<RetailerProduct>>>(
        `/api/v1/retailers/${retailerId}/products`,
        { params }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch products');
      return null;
    }
  }

  /**
   * Add product to retailer catalog
   */
  async addProduct(
    retailerId: number,
    data: ProductListingRequest
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.post<ApiResponse<RetailerProduct>>(
        `/api/v1/retailers/${retailerId}/products`,
        data
      );
      return response.data.data;
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
    data: Partial<ProductListingRequest>
  ): Promise<RetailerProduct | null> {
    try {
      const response = await apiClient.put<ApiResponse<RetailerProduct>>(
        `/api/v1/retailers/${retailerId}/products/${productRetailerId}`,
        data
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to update product');
      return null;
    }
  }

  /**
   * Upload product image
   */
  async uploadProductImage(file: any): Promise<string | null> {
    try {
      const formData = new FormData();
      formData.append('file', {
        uri: file.uri,
        type: file.type || 'image/jpeg',
        name: file.fileName || 'image.jpg',
      } as any);
      
      const response = await apiClient.post<ApiResponse<{ url: string }>>(
        '/api/v1/media/upload',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      return response.data.data.url;
    } catch (error) {
      handleApiError(error, 'Failed to upload image');
      return null;
    }
  }

  /**
   * Get retailer orders
   */
  async getOrders(
    retailerId: number,
    params?: {
      status?: string;
      page?: number;
      size?: number;
    }
  ): Promise<PaginatedResponse<RetailerOrder> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<RetailerOrder>>>(
        `/api/v1/retailers/${retailerId}/orders`,
        { params }
      );
      return response.data.data;
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
        `/api/v1/retailers/${retailerId}/analytics`,
        { params }
      );
      return response.data.data;
    } catch (error) {
      handleApiError(error, 'Failed to fetch analytics');
      return null;
    }
  }
}

export const retailerApi = new RetailerApiService();

