/**
 * Retailer Type Definitions
 * 
 * TypeScript types for Retailer, RetailerProduct, RetailerDashboard, Commission, and RetailerOrder.
 * All types match the backend API DTOs.
 */

// ============================================================================
// Retailer Types
// ============================================================================

/**
 * Retailer status enum
 */
export type RetailerStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED';

/**
 * Retailer entity type matching backend RetailerResponse DTO
 */
export interface Retailer {
  id: number;
  userId: number;
  user?: RetailerUserInfo;
  storeName: string;
  storeSlug: string;
  description?: string;
  logoUrl?: string;
  bannerUrl?: string;
  commissionRate: number;
  status: RetailerStatus;
  rating?: number;
  totalSales?: number;
  totalOrders?: number;
  verified?: boolean;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * Retailer user info type matching backend RetailerResponse.UserInfo
 */
export interface RetailerUserInfo {
  id: number;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
}

// ============================================================================
// Retailer Product Types
// ============================================================================

/**
 * Retailer product type matching backend ProductRetailerResponse DTO
 */
export interface RetailerProduct {
  id: number;
  productId: number;
  product?: ProductInfo;
  retailerId: number;
  retailer?: RetailerInfo;
  price: number;
  compareAtPrice?: number;
  costPrice?: number;
  stockQuantity: number;
  lowStockThreshold: number;
  isActive: boolean;
  isLowStock: boolean;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * Product info type matching backend ProductRetailerResponse.ProductInfo
 */
export interface ProductInfo {
  id: number;
  name: string;
  sku: string;
}

/**
 * Retailer info type matching backend ProductRetailerResponse.RetailerInfo
 */
export interface RetailerInfo {
  id: number;
  storeName: string;
  storeSlug: string;
}

// ============================================================================
// Retailer Dashboard Types
// ============================================================================

/**
 * Retailer dashboard response type matching backend RetailerDashboardResponse DTO
 */
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

/**
 * Recent order type matching backend RetailerDashboardResponse.RecentOrder
 */
export interface RecentOrder {
  orderId: number;
  orderNumber: string;
  totalAmount: number;
  status: string;
  createdAt: string; // ISO date string
}

/**
 * Top product type matching backend RetailerDashboardResponse.TopProduct
 */
export interface TopProduct {
  productId: number;
  productName: string;
  quantitySold: number;
  totalRevenue: number;
}

// ============================================================================
// Commission Types
// ============================================================================

/**
 * Commission tracking type
 */
export interface Commission {
  id: number;
  retailerId: number;
  orderId: number;
  orderNumber: string;
  orderTotal: number;
  commissionRate: number;
  commissionAmount: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  paidAt?: string; // ISO date string
  createdAt: string; // ISO date string
}

/**
 * Commission report type
 */
export interface CommissionReport {
  totalCommissions: number;
  pendingCommissions: number;
  paidCommissions: number;
  cancelledCommissions: number;
  commissions: Commission[];
  period: {
    startDate: string; // ISO date string
    endDate: string; // ISO date string
  };
}

// ============================================================================
// Retailer Order Types
// ============================================================================

/**
 * Retailer order type (extends OrderResponse with retailer-specific fields)
 */
export interface RetailerOrder {
  id: number;
  code: string; // orderNumber
  userId?: number;
  businessAccountId?: number;
  orderType: 'B2C' | 'B2B' | 'RETAILER';
  status: string;
  paymentStatus?: string;
  paymentMethod?: string;
  amount: number; // totalAmount
  subTotal: number;
  taxAmount: number;
  shippingAmount: number;
  discountAmount: number;
  items: RetailerOrderItem[];
  shippingAddress?: any; // AddressResponse
  billingAddress?: any; // AddressResponse
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * Retailer order item type
 */
export interface RetailerOrderItem {
  id: number;
  productId: number;
  retailerId?: number;
  productName: string;
  productSku: string;
  quantity: number;
  price: number;
  total: number;
  commissionAmount?: number; // Platform commission for marketplace orders
}

// ============================================================================
// Request Types
// ============================================================================

/**
 * Retailer registration request type matching backend RetailerRequest DTO
 */
export interface RetailerRegistrationRequest {
  storeName: string;
  storeSlug: string;
  description?: string;
  logoUrl?: string;
  bannerUrl?: string;
  commissionRate?: number;
  bankAccount?: BankAccountDetails;
  taxInformation?: TaxInformation;
}

/**
 * Bank account details for retailer registration
 */
export interface BankAccountDetails {
  accountHolderName: string;
  accountNumber: string;
  bankName: string;
  ifscCode?: string;
  routingNumber?: string;
  swiftCode?: string;
}

/**
 * Tax information for retailer registration
 */
export interface TaxInformation {
  taxId?: string;
  taxNumber?: string;
  vatNumber?: string;
  gstNumber?: string;
}

/**
 * Product listing request type matching backend ProductRetailerRequest DTO
 */
export interface ProductListingRequest {
  productId: number;
  retailerId?: number; // Optional, will be set from auth context
  price: number;
  compareAtPrice?: number;
  costPrice?: number;
  stockQuantity?: number;
  lowStockThreshold?: number;
  isActive?: boolean;
}

/**
 * Update product listing request
 */
export interface UpdateProductListingRequest {
  price?: number;
  compareAtPrice?: number;
  costPrice?: number;
  stockQuantity?: number;
  lowStockThreshold?: number;
  isActive?: boolean;
}

// ============================================================================
// Response Types
// ============================================================================

/**
 * Retailer response type matching backend ApiResponse<RetailerResponse>
 */
export interface RetailerResponse {
  success: boolean;
  message?: string;
  data: Retailer;
  timestamp?: string;
}

/**
 * Retailer dashboard response type matching backend ApiResponse<RetailerDashboardResponse>
 */
export interface RetailerDashboardResponse {
  success: boolean;
  message?: string;
  data: RetailerDashboard;
  timestamp?: string;
}

/**
 * Retailer analytics response type
 */
export interface RetailerAnalyticsResponse {
  success: boolean;
  message?: string;
  data: RetailerAnalytics;
  timestamp?: string;
}

/**
 * Retailer analytics data
 */
export interface RetailerAnalytics {
  salesOverTime: SalesDataPoint[];
  topProducts: TopProduct[];
  revenueByCategory: CategoryRevenue[];
  customerDemographics: CustomerDemographic[];
  commissionBreakdown: CommissionBreakdown;
  payoutHistory: Payout[];
}

/**
 * Sales data point for charts
 */
export interface SalesDataPoint {
  date: string; // ISO date string or label
  sales: number;
  orders: number;
  revenue: number;
}

/**
 * Category revenue data
 */
export interface CategoryRevenue {
  categoryId: number;
  categoryName: string;
  revenue: number;
  orderCount: number;
}

/**
 * Customer demographic data
 */
export interface CustomerDemographic {
  segment: string; // e.g., "18-25", "26-35", etc.
  count: number;
  totalSpent: number;
}

/**
 * Commission breakdown data
 */
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

/**
 * Payout history entry
 */
export interface Payout {
  id: number;
  retailerId: number;
  amount: number;
  status: 'PENDING' | 'PROCESSED' | 'FAILED';
  processedAt?: string; // ISO date string
  createdAt: string; // ISO date string
  description?: string;
}

// ============================================================================
// All types are exported above using 'export interface' and 'export type'
// ============================================================================

