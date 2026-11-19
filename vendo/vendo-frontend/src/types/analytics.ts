/**
 * Analytics Type Definitions
 * 
 * TypeScript types for Analytics tracking, dashboard, and reporting.
 * All types match the backend API DTOs.
 */

// ============================================================================
// Analytics Event Types
// ============================================================================

/**
 * Analytics event type enum
 */
export type AnalyticsEventType =
  | 'PAGE_VIEW'
  | 'PRODUCT_VIEW'
  | 'CART_ADDITION'
  | 'CART_REMOVAL'
  | 'PURCHASE'
  | 'SEARCH'
  | 'FILTER'
  | 'SIGNUP'
  | 'LOGIN'
  | 'LOGOUT'
  | 'CHECKOUT_START'
  | 'CHECKOUT_COMPLETE'
  | 'WISHLIST_ADD'
  | 'WISHLIST_REMOVE'
  | 'REVIEW_SUBMIT'
  | 'COUPON_APPLY'
  | 'CUSTOM';

/**
 * Analytics event properties
 */
export interface AnalyticsEventProperties {
  [key: string]:
    | string
    | number
    | boolean
    | null
    | undefined
    | object; // nested JSON-serializable values (arrays, objects)
}

/**
 * Track event request
 */
export interface TrackEventRequest {
  eventType: AnalyticsEventType | string;
  sessionId?: string;
  properties?: AnalyticsEventProperties;
}

// ============================================================================
// Analytics Dashboard Types
// ============================================================================

/**
 * Sales analytics type matching backend SalesAnalytics DTO
 */
export interface SalesAnalytics {
  totalRevenue: number;
  todayRevenue: number;
  totalOrders: number;
  todayOrders: number;
  averageOrderValue: number;
  revenueByPeriod: RevenueDataPoint[];
}

/**
 * Revenue data point for charts
 */
export interface RevenueDataPoint {
  period: string; // e.g., "2024-01", "2024-01-15"
  revenue: number;
  orders: number;
  date?: string; // ISO date string
}

/**
 * User analytics type matching backend UserAnalytics DTO
 */
export interface UserAnalytics {
  totalUsers: number;
  newUsersToday: number;
  activeUsers: number;
  userGrowth: UserGrowthDataPoint[];
}

/**
 * User growth data point
 */
export interface UserGrowthDataPoint {
  period: string;
  newUsers: number;
  activeUsers: number;
  date?: string; // ISO date string
}

/**
 * Product analytics type matching backend ProductAnalytics DTO
 */
export interface ProductAnalytics {
  totalProducts: number;
  topSellingProducts: number;
  productViews: ProductViewDataPoint[];
  productSales: ProductSalesDataPoint[];
}

/**
 * Product view data point
 */
export interface ProductViewDataPoint {
  productId: number;
  productName: string;
  views: number;
  date?: string; // ISO date string
}

/**
 * Product sales data point
 */
export interface ProductSalesDataPoint {
  productId: number;
  productName: string;
  quantitySold: number;
  revenue: number;
  date?: string; // ISO date string
}

/**
 * Analytics dashboard response type matching backend AnalyticsDashboardResponse DTO
 */
export interface AnalyticsDashboard {
  sales: SalesAnalytics;
  users: UserAnalytics;
  products: ProductAnalytics;
  recentEvents: AnalyticsEvent[];
}

/**
 * Analytics event from dashboard
 */
export interface AnalyticsEvent {
  id?: number;
  eventType: string;
  userId?: number;
  sessionId?: string;
  properties?: Record<string, any>;
  createdAt?: string; // ISO date string
}

/**
 * Analytics dashboard response
 */
export interface AnalyticsDashboardResponse {
  success: boolean;
  message?: string;
  data: AnalyticsDashboard;
  timestamp?: string;
}

// ============================================================================
// Conversion Rate Types
// ============================================================================

/**
 * Conversion rate metrics
 */
export interface ConversionRateMetrics {
  overallConversionRate: number; // Percentage
  cartToCheckoutRate: number; // Percentage
  checkoutToPurchaseRate: number; // Percentage
  productViewToCartRate: number; // Percentage
  byCategory: CategoryConversionRate[];
  byPeriod: PeriodConversionRate[];
}

/**
 * Category conversion rate
 */
export interface CategoryConversionRate {
  categoryId: number;
  categoryName: string;
  conversionRate: number;
  views: number;
  purchases: number;
}

/**
 * Period conversion rate
 */
export interface PeriodConversionRate {
  period: string;
  conversionRate: number;
  views: number;
  purchases: number;
}

// ============================================================================
// Top Customers Types
// ============================================================================

/**
 * Top customer data
 */
export interface TopCustomer {
  userId: number;
  userName: string;
  userEmail: string;
  totalSpent: number;
  orderCount: number;
  averageOrderValue: number;
  lastOrderDate: string; // ISO date string
}

// ============================================================================
// Revenue by Category Types
// ============================================================================

/**
 * Revenue by category data
 */
export interface RevenueByCategory {
  categoryId: number;
  categoryName: string;
  revenue: number;
  orderCount: number;
  productCount: number;
  averageOrderValue: number;
}

// ============================================================================
// All types are exported above using 'export interface' and 'export type'
// ============================================================================

