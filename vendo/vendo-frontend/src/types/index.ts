/**
 * Core TypeScript type definitions for Vendo Frontend
 * 
 * This file contains all type definitions matching the backend entities and DTOs.
 * All types are exported for use in components and services.
 */

// ============================================================================
// User Types
// ============================================================================

/**
 * User entity type matching backend User entity
 */
export interface User {
  id: number;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  userType: 'CUSTOMER' | 'BUSINESS' | 'RETAILER';
  isActive: boolean;
  isVerified: boolean;
  emailVerifiedAt?: string; // ISO date string
  superUser: boolean;
  manageSupers: boolean;
  permissions?: string;
  lastLogin?: string; // ISO date string
  avatarId?: number;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * User DTO used in AuthResponse
 */
export interface UserDto {
  id: number;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  superUser: boolean;
  isVerified: boolean;
  emailVerifiedAt?: string; // ISO date string
  isActive: boolean;
  userType: string;
}

/**
 * User profile response type matching backend UserProfileResponse DTO
 */
export interface UserProfileResponse {
  id: number;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  userType: string;
  isActive: boolean;
  isVerified: boolean;
  emailVerifiedAt?: string; // ISO date string
  lastLogin?: string; // ISO date string
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
  avatar?: any; // MediaFileResponse
  avatarUrl?: string;
  notificationPreferences?: any; // NotificationPreferenceResponse
}

// ============================================================================
// Product Types
// ============================================================================

/**
 * Product entity type matching backend Product entity
 */
export interface Product {
  id: number;
  name: string;
  slug: string;
  description?: string;
  content?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  images?: string; // JSON string or comma-separated URLs
  videoMedia?: string;
  sku: string;
  barcode?: string;
  productType: 'SIMPLE' | 'VARIABLE' | 'GROUPED' | 'DIGITAL' | 'PHYSICAL';
  isFeatured: boolean;
  weight?: number;
  dimensions?: string; // JSON: {length, width, height, unit}
  brandId?: number;
  isVariation: boolean;
  saleType: number;
  price?: number;
  salePrice?: number;
  startDate?: string; // ISO date string
  endDate?: string; // ISO date string
  length?: number;
  wide?: number;
  height?: number;
  taxId?: number;
  views: number;
  storeId?: number;
  createdById: number;
  createdByType: string;
  approvedBy?: number;
  image?: string;
  costPerItem?: number;
  generateLicenseCode: boolean;
  licenseCodeType: string;
  minimumOrderQuantity: number;
  maximumOrderQuantity: number;
  notifyAttachmentUpdated: boolean;
  specificationTableId?: number;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
  categoryIds?: number[];
  categoryNames?: string[];
  averageRating?: number;
  reviewCount?: number;
}

/**
 * Product response type matching backend ProductResponse DTO
 */
export interface ProductResponse {
  id: number;
  name: string;
  description?: string;
  content?: string;
  status: string;
  images?: string;
  videoMedia?: string;
  sku: string;
  order?: number;
  quantity?: number;
  allowCheckoutWhenOutOfStock?: boolean;
  withStorehouseManagement?: boolean;
  stockStatus?: string;
  isFeatured: boolean;
  brandId?: number;
  brandName?: string;
  isVariation: boolean;
  saleType: number;
  price?: number;
  salePrice?: number;
  startDate?: string; // ISO date string
  endDate?: string; // ISO date string
  length?: number;
  width?: number;
  height?: number;
  weight?: number;
  taxId?: number;
  categoryIds?: number[];
  categoryNames?: string[];
  tagIds?: number[];
  tagNames?: string[];
  collectionIds?: number[];
  collectionNames?: string[];
  averageRating?: number;
  reviewCount?: number;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

// ============================================================================
// Category Types
// ============================================================================

/**
 * Category entity type matching backend Category entity
 */
export interface Category {
  id: number;
  name: string;
  slug?: string;
  parentId?: number;
  parent?: Category;
  children?: Category[];
  description?: string;
  status: 'published' | 'draft' | 'archived';
  sortOrder: number;
  isActive: boolean;
  imageUrl?: string;
  createdAt: string; // ISO date string
}

/**
 * Category response type matching backend CategoryResponse DTO
 */
export interface CategoryResponse {
  id: number;
  name: string;
  parentId?: number;
  parentName?: string;
  children?: CategoryResponse[];
  description?: string;
  status: string;
  order?: number;
  isFeatured?: boolean;
  image?: string;
  productCount?: number;
  createdAt?: string; // ISO date string
  updatedAt?: string; // ISO date string
}

// ============================================================================
// Address Types
// ============================================================================

/**
 * Address entity type matching backend Address entity
 */
export interface Address {
  id: number;
  userId?: number;
  businessAccountId?: number;
  addressType?: 'SHIPPING' | 'BILLING' | 'BOTH';
  contactName?: string;
  phone?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  createdAt: string; // ISO date string
}

/**
 * Address response type matching backend AddressResponse DTO
 */
export interface AddressResponse {
  id: number;
  userId?: number;
  addressType?: string;
  contactName?: string;
  phone?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  formattedAddress?: string;
  createdAt: string; // ISO date string
}

// ============================================================================
// Review Types
// ============================================================================

/**
 * Product Review entity type matching backend ProductReview entity
 */
export interface Review {
  id: number;
  productId: number;
  product?: Product;
  retailerId?: number;
  userId?: number;
  user?: User;
  orderId?: number;
  order?: Order;
  rating: number; // 1-5
  title?: string;
  reviewText?: string;
  isVerifiedPurchase: boolean;
  isApproved: boolean;
  helpfulCount: number;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * Review response type matching backend ReviewResponse DTO
 */
export interface ReviewResponse {
  id: number;
  productId: number;
  productName?: string;
  userId?: number;
  userName?: string;
  userEmail?: string;
  orderId?: number;
  rating: number;
  title?: string;
  reviewText?: string;
  isVerifiedPurchase: boolean;
  isApproved: boolean;
  helpfulCount: number;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

// ============================================================================
// Wishlist Types
// ============================================================================

/**
 * Wishlist entity type matching backend Wishlist entity
 * Uses composite primary key (customerId, productId)
 */
export interface Wishlist {
  customerId: number;
  productId: number;
  product?: Product;
  customer?: User;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

/**
 * Wishlist response type matching backend WishlistResponse DTO
 */
export interface WishlistResponse {
  customerId: number;
  productId: number;
  product?: ProductResponse;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

// ============================================================================
// Coupon Types
// ============================================================================

/**
 * Coupon entity type matching backend Coupon entity
 */
export interface Coupon {
  id: number;
  code: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING';
  discountValue: number;
  minimumPurchase?: number;
  maximumDiscount?: number;
  usageLimit?: number;
  usedCount: number;
  validFrom: string; // ISO date string
  validUntil: string; // ISO date string
  applicableTo?: 'ALL' | 'B2C' | 'B2B' | 'SPECIFIC_PRODUCTS' | 'SPECIFIC_CATEGORIES';
  isActive: boolean;
  createdAt: string; // ISO date string
  productIds?: number[];
  categoryIds?: number[];
}

/**
 * Coupon response type matching backend CouponResponse DTO
 */
export interface CouponResponse {
  id: number;
  code: string;
  name: string;
  description?: string;
  discountType: string;
  discountValue: number;
  minimumPurchase?: number;
  maximumDiscount?: number;
  usageLimit?: number;
  usedCount: number;
  validFrom: string; // ISO date string
  validUntil: string; // ISO date string
  applicableTo?: string;
  isActive: boolean;
  createdAt: string; // ISO date string
  productIds?: number[];
  categoryIds?: number[];
  // Discount calculation details (for applyCoupon response)
  calculatedDiscount?: number;
  originalAmount?: number;
  finalAmount?: number;
  isValid?: boolean;
  validationMessage?: string;
}

// ============================================================================
// Order Types
// ============================================================================

/**
 * Order status enum
 */
export type OrderStatus = 
  | 'PENDING' 
  | 'CONFIRMED' 
  | 'PROCESSING' 
  | 'SHIPPED' 
  | 'DELIVERED' 
  | 'CANCELLED' 
  | 'REFUNDED';

/**
 * Payment status enum
 */
export type PaymentStatus = 
  | 'PENDING' 
  | 'PAID' 
  | 'PARTIAL' 
  | 'REFUNDED' 
  | 'FAILED';

/**
 * Order type enum
 */
export type OrderType = 'B2C' | 'B2B' | 'RETAILER';

/**
 * Order entity type matching backend Order entity
 */
export interface Order {
  id: number;
  orderNumber: string;
  userId?: number;
  user?: User;
  businessAccountId?: number;
  orderType: OrderType;
  status: OrderStatus;
  paymentStatus?: PaymentStatus;
  paymentMethod?: string;
  subtotal: number;
  taxAmount: number;
  shippingAmount: number;
  discountAmount: number;
  totalAmount: number;
  currency: string;
  notes?: string;
  cancellationReason?: string;
  cancelledAt?: string; // ISO date string
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
  orderItems?: OrderItem[];
  shippingAddress?: AddressResponse;
  billingAddress?: AddressResponse;
}

/**
 * Order item type matching backend OrderItem entity
 */
export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  productName?: string;
  productSku?: string;
  quantity: number;
  price: number;
  total: number;
}

/**
 * Order item response type matching backend OrderItemResponse DTO
 */
export interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  price: number;
  total: number;
}

/**
 * Order response type matching backend OrderResponse DTO
 */
export interface OrderResponse {
  id: number;
  code: string; // orderNumber
  userId?: number;
  status: string;
  amount: number; // totalAmount
  subTotal: number;
  taxAmount: number;
  shippingAmount: number;
  discountAmount: number;
  paymentFee?: number;
  shippingMethod?: string;
  couponCode?: string;
  description?: string;
  isConfirmed?: boolean;
  isFinished?: boolean;
  completedAt?: string; // ISO date string
  items: OrderItemResponse[];
  shippingAddress?: AddressResponse;
  billingAddress?: AddressResponse;
  createdAt: string; // ISO date string
  updatedAt: string; // ISO date string
}

// ============================================================================
// Order Cancellation Types
// ============================================================================

/**
 * Cancellation reason enum matching backend CancellationReason enum
 */
export type CancellationReason =
  | 'CUSTOMER_REQUESTED'
  | 'OUT_OF_STOCK'
  | 'PRICE_MISMATCH'
  | 'DUPLICATE_ORDER'
  | 'WRONG_ADDRESS'
  | 'PAYMENT_FAILED'
  | 'FRAUDULENT_ORDER'
  | 'CUSTOMER_CHANGED_MIND'
  | 'DELIVERY_DELAY'
  | 'PRODUCT_DAMAGED'
  | 'OTHER';

/**
 * Cancel order request type matching backend CancelOrderRequest DTO
 */
export interface CancelOrderRequest {
  reason: CancellationReason;
  customReason?: string; // For "OTHER" reason type
}

/**
 * Cancellation response type matching backend CancellationResponse DTO
 */
export interface CancellationResponse {
  orderId: number;
  orderNumber: string;
  status: string;
  cancellationReason: string;
  cancelledAt?: string; // ISO date string
  refundProcessed: boolean;
  refundAmount?: number;
  refundStatus?: string;
  refundTransactionId?: string;
  inventoryUpdated: boolean;
  message: string;
}

// ============================================================================
// Request Types
// ============================================================================

/**
 * Registration request type matching backend RegisterRequest DTO
 */
export interface RegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  username?: string;
  phone?: string;
}

/**
 * Login request type matching backend LoginRequest DTO
 */
export interface LoginRequest {
  emailOrUsername: string;
  password: string;
}

/**
 * Product search request type for product search endpoints
 * Matches the search parameters used in ProductController
 */
export interface ProductSearchRequest {
  keyword: string;
  status?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
  categoryId?: number;
  brandId?: number;
  minPrice?: number;
  maxPrice?: number;
}

/**
 * Update profile request type matching backend UpdateProfileRequest DTO
 */
export interface UpdateProfileRequest {
  email?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

/**
 * Change password request type matching backend ChangePasswordRequest DTO
 */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

/**
 * Review request type matching backend ReviewRequest DTO
 */
export interface ReviewRequest {
  productId: number;
  orderId?: number; // Optional, for verified purchase
  rating: number; // 1-5
  title?: string;
  reviewText?: string;
}

// ============================================================================
// Response Types
// ============================================================================

/**
 * Authentication response type matching backend AuthResponse DTO
 */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresAt: string; // ISO date string
  user: UserDto;
}

/**
 * Generic API response wrapper matching backend ApiResponse<T>
 */
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}

/**
 * Paginated response type for paginated API endpoints
 */
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page (0-indexed)
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

// ============================================================================
// Cart Types
// ============================================================================

/**
 * Cart item type matching backend CartItemResponse DTO
 */
export interface CartItem {
  productId: number;
  productName: string;
  productSku: string;
  productImage?: string;
  price: number;
  salePrice?: number;
  quantity: number;
  itemTotal: number;
  variationId?: number;
}

/**
 * Cart response type matching backend CartResponse DTO
 */
export interface Cart {
  userId?: number;
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  updatedAt: string; // ISO date string
}

/**
 * Shipping method type
 */
export interface ShippingMethod {
  id: number;
  name: string;
  code: string;
  description?: string;
  carrier?: string;
  baseCost: number;
  costPerKg?: number;
  estimatedDays?: number;
  isActive: boolean;
  createdAt: string; // ISO date string
}

/**
 * Payment method enum
 */
export type PaymentMethod = 
  | 'CREDIT_CARD' 
  | 'DEBIT_CARD' 
  | 'PAYPAL' 
  | 'STRIPE' 
  | 'CASH_ON_DELIVERY'
  | 'BANK_TRANSFER';

// ============================================================================
// All types are exported above using 'export interface' and 'export type'
// No additional export block needed - types are already exported individually
// ============================================================================
