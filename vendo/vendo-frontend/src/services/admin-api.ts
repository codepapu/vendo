import { apiClient, handleApiError, showSuccessToast } from '@/lib/api';
import type {
  ApiResponse,
  PaginatedResponse,
  ProductResponse,
  OrderResponse,
  CouponResponse,
} from '@/types';

/**
 * Admin User Management Response
 */
export interface AdminUserResponse {
  id: number;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  userType: string;
  isActive: boolean;
  isVerified: boolean;
  roles?: string[];
  registrationDate: string;
  lastLogin?: string;
  orderCount?: number;
  totalSpent?: number;
}

/**
 * System Configuration Response
 */
export interface SystemConfigResponse {
  paymentGateways?: Record<string, any>;
  shippingMethods?: Record<string, any>;
  taxSettings?: Record<string, any>;
  emailTemplates?: Record<string, any>;
  systemSettings?: Record<string, any>;
}

/**
 * Admin API service
 * Handles all admin-related API calls
 */
class AdminApiService {
  // ============================================================================
  // Product Management
  // ============================================================================

  /**
   * Get all products for admin (with all statuses)
   */
  async getAllProducts(params?: {
    status?: string;
    search?: string;
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
   * Bulk update product status
   */
  async bulkUpdateProductStatus(
    productIds: number[],
    status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  ): Promise<boolean> {
    try {
      const response = await apiClient.put<ApiResponse<void>>(
        '/v1/products/bulk-status',
        { productIds, status }
      );
      
      if (response.data.success) {
        showSuccessToast('Products updated successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to update products');
      return false;
    }
  }

  /**
   * Bulk delete products
   */
  async bulkDeleteProducts(productIds: number[]): Promise<boolean> {
    try {
      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/products/bulk-delete',
        { productIds }
      );
      
      if (response.data.success) {
        showSuccessToast('Products deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete products');
      return false;
    }
  }

  /**
   * Export products to CSV
   */
  async exportProducts(params?: {
    status?: string;
    search?: string;
  }): Promise<Blob | null> {
    try {
      const response = await apiClient.get('/v1/products/export', {
        params,
        responseType: 'blob',
      });
      
      return response.data;
    } catch (error) {
      handleApiError(error, 'Failed to export products');
      return null;
    }
  }

  /**
   * Import products from CSV
   */
  async importProducts(file: File): Promise<boolean> {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await apiClient.post<ApiResponse<void>>(
        '/v1/products/import',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      if (response.data.success) {
        showSuccessToast('Products imported successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to import products');
      return false;
    }
  }

  // ============================================================================
  // Order Management
  // ============================================================================

  /**
   * Get all orders for admin
   */
  async getAllOrders(params?: {
    status?: string;
    orderType?: string;
    customerId?: number;
    dateFrom?: string;
    dateTo?: string;
    search?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<OrderResponse> | null> {
    try {
      // Use regular orders endpoint - admin can access all orders
      const response = await apiClient.get<ApiResponse<PaginatedResponse<OrderResponse>>>(
        '/v1/orders',
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
   * Update order status
   */
  async updateOrderStatus(orderId: number, status: string): Promise<boolean> {
    try {
      // Try admin endpoint first, fallback to regular endpoint
      try {
        const response = await apiClient.put<ApiResponse<void>>(
          `/v1/admin/orders/${orderId}/status`,
          { status }
        );
        
        if (response.data.success) {
          showSuccessToast('Order status updated successfully');
          return true;
        }
      } catch {
        // Fallback: use order update endpoint if admin endpoint doesn't exist
        const response = await apiClient.put<ApiResponse<void>>(
          `/v1/orders/${orderId}`,
          { status }
        );
        
        if (response.data.success) {
          showSuccessToast('Order status updated successfully');
          return true;
        }
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to update order status');
      return false;
    }
  }

  /**
   * Bulk update order status
   */
  async bulkUpdateOrderStatus(orderIds: number[], status: string): Promise<boolean> {
    try {
      // Try admin endpoint first, fallback to individual updates
      try {
        const response = await apiClient.put<ApiResponse<void>>(
          '/v1/admin/orders/bulk-status',
          { orderIds, status }
        );
        
        if (response.data.success) {
          showSuccessToast('Orders updated successfully');
          return true;
        }
      } catch {
        // Fallback: update orders individually
        const promises = orderIds.map(id => this.updateOrderStatus(id, status));
        await Promise.all(promises);
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to update orders');
      return false;
    }
  }

  /**
   * Cancel order
   */
  async cancelOrder(orderId: number, reason?: string): Promise<boolean> {
    try {
      // Use order cancellation endpoint
      const response = await apiClient.post<ApiResponse<void>>(
        `/v1/orders/${orderId}/cancel`,
        { reason: reason || 'ADMIN_CANCELLED' }
      );
      
      if (response.data.success) {
        showSuccessToast('Order cancelled successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to cancel order');
      return false;
    }
  }

  /**
   * Process refund
   */
  async processRefund(orderId: number, amount?: number, reason?: string): Promise<boolean> {
    try {
      // Use payment refund endpoint
      const response = await apiClient.post<ApiResponse<void>>(
        `/v1/payments/refund`,
        { orderId, amount, reason: reason || 'Admin refund' }
      );
      
      if (response.data.success) {
        showSuccessToast('Refund processed successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to process refund');
      return false;
    }
  }

  // ============================================================================
  // User Management
  // ============================================================================

  /**
   * Get all users for admin
   */
  async getAllUsers(params?: {
    userType?: string;
    status?: string;
    search?: string;
    dateFrom?: string;
    dateTo?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<AdminUserResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<AdminUserResponse>>>(
        '/v1/admin/users',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch users');
      return null;
    }
  }

  /**
   * Get user by ID with details
   */
  async getUserById(userId: number): Promise<AdminUserResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<AdminUserResponse>>(
        `/v1/admin/users/${userId}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch user');
      return null;
    }
  }

  /**
   * Get user order history
   */
  async getUserOrderHistory(userId: number): Promise<OrderResponse[]> {
    try {
      const response = await apiClient.get<ApiResponse<OrderResponse[]>>(
        `/v1/admin/users/${userId}/orders`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return [];
    } catch (error) {
      handleApiError(error, 'Failed to fetch user orders');
      return [];
    }
  }

  /**
   * Update user
   */
  async updateUser(userId: number, data: {
    firstName?: string;
    lastName?: string;
    phone?: string;
    isActive?: boolean;
  }): Promise<AdminUserResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<AdminUserResponse>>(
        `/v1/admin/users/${userId}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('User updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update user');
      return null;
    }
  }

  /**
   * Update user roles
   */
  async updateUserRoles(userId: number, roles: string[]): Promise<boolean> {
    try {
      const response = await apiClient.put<ApiResponse<void>>(
        `/v1/admin/users/${userId}/roles`,
        roles
      );
      
      if (response.data.success) {
        showSuccessToast('User roles updated successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to update user roles');
      return false;
    }
  }

  /**
   * Suspend user
   */
  async suspendUser(userId: number): Promise<boolean> {
    try {
      const response = await apiClient.put<ApiResponse<void>>(
        `/v1/admin/users/${userId}/suspend`,
        {}
      );
      
      if (response.data.success) {
        showSuccessToast('User suspended successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to suspend user');
      return false;
    }
  }

  /**
   * Delete user
   */
  async deleteUser(userId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/admin/users/${userId}`
      );
      
      if (response.data.success) {
        showSuccessToast('User deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete user');
      return false;
    }
  }

  // ============================================================================
  // System Configuration
  // ============================================================================

  /**
   * Get system configuration
   */
  async getSystemConfiguration(): Promise<SystemConfigResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<SystemConfigResponse>>(
        '/v1/admin/config'
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch system configuration');
      return null;
    }
  }

  /**
   * Update system configuration
   */
  async updateSystemConfiguration(config: SystemConfigResponse): Promise<boolean> {
    try {
      const response = await apiClient.put<ApiResponse<void>>(
        '/v1/admin/config',
        config
      );
      
      if (response.data.success) {
        showSuccessToast('System configuration updated successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to update system configuration');
      return false;
    }
  }

  // ============================================================================
  // Coupon Management
  // ============================================================================

  /**
   * Get all coupons for admin
   */
  async getAllCoupons(params?: {
    status?: string;
    search?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<CouponResponse> | null> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<CouponResponse>>>(
        '/v1/coupons',
        { params }
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch coupons');
      return null;
    }
  }

  /**
   * Get coupon by ID
   */
  async getCouponById(couponId: number): Promise<CouponResponse | null> {
    try {
      const response = await apiClient.get<ApiResponse<CouponResponse>>(
        `/v1/coupons/${couponId}`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch coupon');
      return null;
    }
  }

  /**
   * Create coupon
   */
  async createCoupon(data: {
    code: string;
    name: string;
    description?: string;
    discountType: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING';
    discountValue: number;
    minimumPurchase?: number;
    maximumDiscount?: number;
    usageLimit?: number;
    validFrom: string;
    validUntil: string;
    applicableTo?: string;
    productIds?: number[];
    categoryIds?: number[];
  }): Promise<CouponResponse | null> {
    try {
      const response = await apiClient.post<ApiResponse<CouponResponse>>(
        '/v1/coupons',
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Coupon created successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to create coupon');
      return null;
    }
  }

  /**
   * Update coupon
   */
  async updateCoupon(couponId: number, data: Partial<{
    name: string;
    description?: string;
    discountType: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING';
    discountValue: number;
    minimumPurchase?: number;
    maximumDiscount?: number;
    usageLimit?: number;
    validFrom: string;
    validUntil: string;
    applicableTo?: string;
    isActive: boolean;
    productIds?: number[];
    categoryIds?: number[];
  }>): Promise<CouponResponse | null> {
    try {
      const response = await apiClient.put<ApiResponse<CouponResponse>>(
        `/v1/coupons/${couponId}`,
        data
      );
      
      if (response.data.success && response.data.data) {
        showSuccessToast('Coupon updated successfully');
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to update coupon');
      return null;
    }
  }

  /**
   * Delete coupon
   */
  async deleteCoupon(couponId: number): Promise<boolean> {
    try {
      const response = await apiClient.delete<ApiResponse<void>>(
        `/v1/coupons/${couponId}`
      );
      
      if (response.data.success) {
        showSuccessToast('Coupon deleted successfully');
        return true;
      }
      
      return false;
    } catch (error) {
      handleApiError(error, 'Failed to delete coupon');
      return false;
    }
  }

  /**
   * Get coupon analytics
   */
  async getCouponAnalytics(couponId: number): Promise<any> {
    try {
      const response = await apiClient.get<ApiResponse<any>>(
        `/v1/coupons/${couponId}/analytics`
      );
      
      if (response.data.success && response.data.data) {
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      handleApiError(error, 'Failed to fetch coupon analytics');
      return null;
    }
  }
}

export const adminApi = new AdminApiService();
export default adminApi;
