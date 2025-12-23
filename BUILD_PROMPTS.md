# Step-by-Step Build Prompts

Use these prompts one at a time. Copy and paste each prompt when you're ready to implement that feature.

## Phase 0: Core B2C Database & Entities

### Prompt 1: Create Core Database Tables
```
Create database migration for core B2C functionality in vendo-backend/src/main/resources/db/migration/. 
Add tables: users, products, categories, product_category_product (join table), product_variations, product_variation_items, product_attributes, addresses, shipping_methods, coupons, wishlists, product_reviews. 
Include all foreign keys, indexes, and constraints as specified in DATABASE_SCHEMA.md. 
Use Flyway naming convention V1__create_core_tables.sql. 
Ensure all columns match the schema documentation exactly.
```

### Prompt 2: Create JPA Entity Classes for Core Entities
```
Create JPA entity classes in vendo-backend for core functionality: User, Product, Category, ProductVariation, ProductVariationItem, ProductAttribute, Address, ShippingMethod, Coupon, Wishlist, ProductReview. 
Add proper annotations (@Entity, @Table, @Id, @ManyToOne, @OneToMany, @ManyToMany, @JoinTable, @JoinColumn, @GeneratedValue, @Column). 
Include validation annotations (@NotNull, @Size, @Email, @Min, @Max). 
Add Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor). 
Add @EntityListeners(AuditingEntityListener.class) and use @CreatedDate/@LastModifiedDate for audit fields.
Ensure bidirectional relationships are properly mapped with appropriate fetch types (LAZY for @ManyToOne/@OneToMany, EAGER only when necessary).

Entity-specific requirements:

1. User:
   - Fields: id, email (unique), username (unique), password_hash, first_name, last_name, phone, user_type, is_active, is_verified, last_login, created_at, updated_at
   - Add @ManyToMany relationship to Role entity using @JoinTable(name = "role_users")
   - Add @OneToMany for addresses, orders, reviews, wishlists
   - Add indexes on email and username

2. Product:
   - Fields: id, name, slug (unique), description, sku (unique), barcode, product_type, status, is_featured, weight, dimensions (JSONB), created_at, updated_at
   - Add @ManyToMany relationship to Category using @JoinTable(name = "product_category_product")
   - Add @OneToMany for ProductVariation (both as product and configurableProduct), ProductReview, ProductAttribute
   - Add indexes on slug, status, sku, is_featured

3. Category:
   - Fields: id, name, slug (unique), description, parent_id, image_url, is_active, sort_order, created_at
   - Add @ManyToOne to Category (self-referencing for parent)
   - Add @OneToMany(mappedBy = "parent") for children categories with cascade ALL
   - Add @ManyToMany(mappedBy = "categories") for products
   - Add indexes on parent_id, status, sort_order

4. ProductVariation:
   - Fields: id, product_id, configurable_product_id (NOT NULL), is_default, created_at, updated_at
   - Add @ManyToOne to Product (as variation product)
   - Add @ManyToOne to Product (as configurableProduct) with nullable = false
   - Add @OneToMany(mappedBy = "variation") for ProductVariationItem list with cascade ALL and orphanRemoval
   - Add unique constraint on (product_id, configurable_product_id)
   - Add indexes on (product_id, configurable_product_id) and configurable_product_id

5. ProductVariationItem:
   - Fields: id, variation_id (NOT NULL), attribute_id (NOT NULL), created_at, updated_at
   - Add @ManyToOne to ProductVariation with nullable = false
   - Add @ManyToOne to ProductAttribute with nullable = false
   - Add indexes on variation_id and attribute_id

6. ProductAttribute:
   - Fields: id, product_id, attribute_name, attribute_value, created_at
   - Add @ManyToOne to Product with ON DELETE CASCADE
   - Add @OneToMany for ProductVariationItem if needed

7. Wishlist:
   - Use @IdClass(WishlistId.class) for composite primary key (customer_id, product_id)
   - Fields: customer_id (PK), product_id (PK), created_at, updated_at
   - Create inner static class WishlistId implementing Serializable with customerId and productId
   - Add @ManyToOne to Product (read-only, insertable = false, updatable = false)
   - Add @ManyToOne to User (customer)
   - Add indexes on (product_id, customer_id) and customer_id

8. ProductReview:
   - Fields: id, product_id, user_id, order_id (optional), rating (1-5), title, review_text, is_verified_purchase, is_approved, helpful_count, created_at, updated_at
   - Add @ManyToOne to Product with nullable = false
   - Add @ManyToOne to User
   - Add @ManyToOne to Order (optional, for verified purchase)
   - Add unique constraint on (product_id, user_id) if one review per user per product
   - Add indexes on product_id, user_id, status
   - Add @Min(1) @Max(5) validation on rating

9. Address:
   - Fields: id, user_id, business_account_id (optional), address_type, contact_name, phone, address_line1, address_line2, city, state, postal_code, country, is_default, created_at
   - Add @ManyToOne to User
   - Add @ManyToOne to BusinessAccount (optional)
   - Add indexes on user_id, business_account_id, is_default

10. ShippingMethod:
    - Fields: id, name, code (unique), description, carrier, base_cost, cost_per_kg, estimated_days, is_active, created_at
    - Add @OneToMany for shipments if needed
    - Add index on code and is_active

11. Coupon:
    - Fields: id, code (unique), name, description, discount_type, discount_value, minimum_purchase, maximum_discount, usage_limit, used_count, valid_from, valid_until, applicable_to, is_active, created_at
    - Add @ManyToMany to Product and Category if applicable_to is SPECIFIC_PRODUCTS or SPECIFIC_CATEGORIES
    - Add @NotNull validation on discount_type, discount_value, valid_from, valid_until
    - Add @Min(0) validation on discount_value, minimum_purchase, maximum_discount
    - Add index on code, is_active, valid_from, valid_until
    - Add validation to ensure valid_until > valid_from

Add @Table annotations with indexes and unique constraints as specified in DATABASE_SCHEMA.md.
Use @Builder.Default for fields with default values.
Use appropriate column types: VARCHAR with length, TEXT for long text, DECIMAL(precision, scale) for money, TIMESTAMP for dates.
Add @Column annotations with nullable, unique, length, and columnDefinition where needed.
```

## Phase 0.5: Core B2C Backend Services

### Prompt 3: Create Authentication Service
```
Create AuthService with methods: register, login, logout, refreshToken, verifyEmail, resetPassword, changePassword. 
Implement JWT token generation and validation. 
Add email verification flow. 
Handle password reset with secure tokens. 
Return AuthResponse with access token, refresh token, and user details.
```

### Prompt 3.5: Create Social Login Service
```
Create SocialAuthService to handle OAuth2 social login (Google, Facebook, GitHub). 
Implement OAuth2 user processing and account creation. 
Link social accounts to existing users. 
Generate JWT tokens after social authentication. 
Return AuthResponse with tokens and user details.
```

### Prompt 4: Create User Profile Service
```
Create UserProfileService with methods: getProfile, updateProfile, updatePassword, uploadAvatar, deleteAccount. 
Validate user data before updates. 
Handle file uploads for profile pictures. 
Return UserProfileResponse with user details and preferences.
```

### Prompt 5: Create Address Management Service
```
Create AddressService with methods: addAddress, updateAddress, deleteAddress, getAddresses, setDefaultAddress. 
Validate address data. 
Support multiple addresses per user. 
Return AddressResponse with formatted address.
```

### Prompt 6: Create Product Catalog Service
```
Create ProductCatalogService with methods: getProducts, getProduct, searchProducts, getProductsByCategory, getFeaturedProducts. 
Implement pagination and filtering (price, category, brand, rating). 
Support sorting (price, name, rating, date). 
Return paginated ProductResponse list.
```

### Prompt 6.5: Create Recently Viewed Products Service
```
Create RecentlyViewedService with methods: addToRecentlyViewed, getRecentlyViewed, clearRecentlyViewed. 
Track product views per user with timestamps. 
Limit to last 20 viewed products. 
Return RecentlyViewedResponse with product details and view dates.
```

### Prompt 7: Create Product Review Service
```
Create ProductReviewService with methods: addReview, updateReview, deleteReview, getProductReviews, getReviewStats. 
Validate review ownership. 
Calculate average ratings. 
Require verified purchase for reviews. 
Return ReviewResponse with rating and review text.
```

### Prompt 8: Create Wishlist Service
```
Create WishlistService with methods: addToWishlist, removeFromWishlist, getWishlist, clearWishlist, isInWishlist. 
Track wishlist items per user. 
Return WishlistResponse with product details.
```

### Prompt 9: Create Coupon Service
```
Create CouponService with methods: validateCoupon, applyCoupon, getAvailableCoupons, createCoupon. 
Validate coupon codes, expiration dates, and usage limits. 
Calculate discounts based on coupon type. 
Return CouponResponse with discount details.
```

### Prompt 10: Create Core REST Controllers
```
Create AuthController, UserController, ProductController, ReviewController, WishlistController, and CouponController. 
Add endpoints for all CRUD operations. 
Use @PreAuthorize for authorization. 
Use @Valid for request validation. 
Return ApiResponse wrapper for all responses.
```

### Prompt 10.5: Create Order Cancellation Service
```
Create OrderCancellationService with methods: cancelOrder, getCancellationReasons, processRefund. 
Validate cancellation eligibility (time window, order status). 
Handle refund processing through payment gateway. 
Update inventory when orders are cancelled. 
Return CancellationResponse with status and refund details.
```

## Phase 0.6: Core B2C Frontend

### Prompt 11: Create Core Type Definitions
```
Create TypeScript type definitions in vendo/vendo-frontend/src/types/ for User, Product, Category, Address, Review, Wishlist, Coupon, and Order. 
Add request types: RegisterRequest, LoginRequest, ProductSearchRequest. 
Include response types with all fields from backend. 
Export all types for use in components and services. 
Add JSDoc comments for complex types.
```

### Prompt 12: Create Core API Services
```
Create api services in vendo/vendo-frontend/src/services/: auth-api.ts, user-api.ts, product-api.ts, review-api.ts, wishlist-api.ts. 
Use apiClient from @/lib/api. 
Handle errors with try-catch and show toast notifications. 
Return typed responses using core types. 
Add loading states for async operations.
```

### Prompt 13: Create Auth Store (Zustand)
```
Create auth-store.ts in vendo/vendo-frontend/src/stores/ using Zustand for authentication state management. 
Store: user, accessToken, refreshToken, isAuthenticated. 
Actions: login, logout, register, refreshToken, updateUser. 
Persist tokens to localStorage. 
Add loading and error states.
```

### Prompt 14: Create Product Catalog Pages
```
Create products/index.tsx page showing product catalog with search, filters, and sorting. 
Create products/[id].tsx page showing product details with images, description, reviews, and add to cart. 
Use ProductGrid and ProductCard components. 
Implement pagination. 
Fetch data using TanStack Query.
```

### Prompt 15: Create User Profile Pages
```
Create profile/index.tsx page for user profile management. 
Create profile/addresses.tsx page for address management. 
Create profile/orders.tsx page for order history. 
Create profile/settings.tsx page for account settings. 
Use Form components for editing. 
Add image upload for profile picture.
```

### Prompt 16: Create Cart and Checkout Pages (Basic B2C)
```
Create cart/index.tsx page showing cart items with quantity updates and removal. 
Create checkout/index.tsx page with address selection, shipping method, payment method, and order summary. 
Add coupon code input. 
Show order confirmation after successful checkout. 
Handle guest checkout option.
```

### Prompt 17: Create Order Tracking Page
```
Create orders/[id].tsx page showing order details and tracking information. 
Display order status, items, shipping address, payment info, and tracking number. 
Add order cancellation option for pending orders. 
Show estimated delivery date.
```

### Prompt 17.5: Create Guest Checkout Service
```
Create GuestCheckoutService to handle checkout for non-authenticated users. 
Generate temporary session tokens for guest orders. 
Store guest order information with email. 
Allow guest users to create account after checkout. 
Send order confirmation to guest email.
```

## Phase 0.7: Content Management

### Prompt 18: Create Content Management Service
```
Create ContentService for managing blog posts, pages, and media files. 
Implement BlogService with methods: createPost, updatePost, publishPost, getPosts, getPostBySlug. 
Implement PageService with methods: createPage, updatePage, getPageBySlug. 
Implement MediaService for file uploads and management. 
Return ContentResponse with blog posts, pages, and media URLs.
```

### Prompt 18.5: Create Content Management Frontend
```
Create admin/content/ pages for managing blog posts and pages. 
Create blog/index.tsx and blog/[slug].tsx for public blog viewing. 
Create pages/[slug].tsx for dynamic page rendering. 
Add rich text editor for content creation. 
Support media library for image selection.
```

## Phase 1: Database Setup & Core Entities

### Prompt 19: Create B2B Database Tables
```
Create database migration files for B2B functionality in vendo-backend/src/main/resources/db/migration/. 
Add tables: business_accounts, business_contacts, b2b_pricing_tiers, purchase_orders, and invoices. 
Include all foreign keys, indexes, and constraints as specified in DATABASE_SCHEMA.md. 
Use Flyway naming convention V2__create_b2b_tables.sql. 
Ensure all columns match the schema documentation exactly.
```

### Prompt 20: Create Retailer Database Tables
```
Create database migration files for Retailer/Marketplace functionality. 
Add tables: retailers, retailer_bank_accounts, product_retailers, retailer_reviews, and commission_tracking. 
Include foreign keys to products and users tables. 
Use Flyway naming convention V3__create_retailer_tables.sql. 
Add indexes on retailer_id, product_id, and status columns for performance.
```

### Prompt 21: Create Enhanced Order Tables
```
Create database migration for enhanced order system supporting B2C, B2B, and Retailer orders. 
Add columns to orders table: business_account_id, order_type, payment_status. 
Create order_items table with retailer_id for marketplace support. 
Add shipment tracking table with tracking_number and carrier fields. 
Use migration V4__create_enhanced_orders.sql.
```

### Prompt 22: Create Inventory Management Tables
```
Create database migration for multi-warehouse inventory management. 
Add tables: warehouses, inventory (with warehouse_id and retailer_id), and stock_movements. 
Include available_quantity as computed column (quantity - reserved_quantity). 
Add indexes on product_id, retailer_id, and warehouse_id combinations. 
Use migration V5__create_inventory_tables.sql.
```

### Prompt 23: Create JPA Entity Classes for B2B
```
Create JPA entity classes in vendo-backend for B2B functionality: BusinessAccount, BusinessContact, B2BPricingTier, PurchaseOrder, and Invoice. 
Add proper annotations (@Entity, @Table, @Id, @ManyToOne, @OneToMany). 
Include validation annotations (@NotNull, @Size, @Email). 
Add Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor). 
Ensure bidirectional relationships are properly mapped.
```

### Prompt 24: Create JPA Entity Classes for Retailer
```
Create JPA entity classes for Retailer functionality: Retailer, RetailerBankAccount, ProductRetailer, and RetailerReview. 
Add proper JPA annotations and relationships. 
Include @CreatedDate and @LastModifiedDate for audit fields. 
Add validation constraints for required fields. 
Ensure ProductRetailer has unique constraint on (product_id, retailer_id).
```

## Phase 1.5: Payment & Notification Integration

### Prompt 25: Create Payment Gateway Integration Service
```
Enhance PaymentService to integrate with payment gateways (Stripe, PayPal, Razorpay). 
Implement gateway-specific adapters. 
Handle payment webhooks. 
Process refunds and partial refunds. 
Return PaymentResponse with transaction details.
```

### Prompt 26: Create Payment Frontend Integration
```
Create payment components in vendo/vendo-frontend/src/components/payment/ for different payment methods. 
Implement Stripe Elements integration. 
Add payment method selection UI. 
Handle payment processing and error states. 
Show payment confirmation.
```

### Prompt 27: Create Email Notification Templates
```
Create email templates for: order confirmation, shipping notification, payment receipt, password reset, email verification, order cancellation. 
Use Thymeleaf or similar template engine. 
Support HTML and plain text formats. 
Include branding and dynamic content.
```

### Prompt 28: Create Notification Service Frontend
```
Create notification components in vendo/vendo-frontend/src/components/notifications/. 
Implement toast notifications for success/error messages. 
Add in-app notification center. 
Show notification badges. 
Support real-time notifications via WebSocket.
```

### Prompt 28.5: Create Tax Calculation Service
```
Create TaxService with methods: calculateTax, getTaxRates, getTaxByLocation, applyTaxRules. 
Support different tax calculation methods (by location, product type, business type). 
Integrate with tax calculation APIs if needed. 
Handle tax exemptions for B2B customers. 
Return TaxResponse with tax amount and breakdown.
```

### Prompt 28.6: Create Shipping Method Selection Service
```
Create ShippingMethodService with methods: getAvailableShippingMethods, calculateShippingCost, selectShippingMethod, getShippingEstimates. 
Return available shipping methods based on address and order weight. 
Calculate shipping costs for each method. 
Support multiple shipping addresses per order. 
Return ShippingMethodResponse with options and costs.
```

### Prompt 28.7: Create Notification Preferences Service
```
Create NotificationPreferenceService with methods: getPreferences, updatePreferences, getPreferenceByType. 
Allow users to configure notification channels (email, SMS, push, in-app). 
Support granular preferences per notification type. 
Respect user preferences when sending notifications. 
Return NotificationPreferenceResponse with user settings.
```

## Phase 2: Backend Services - B2B

### Prompt 29: Create B2B Service Layer
```
Create B2BService class in vendo-backend with methods: createBusinessAccount, approveBusinessAccount, getBusinessAccount, updateCreditLimit, and getB2BPricing. 
Inject BusinessAccountRepository and B2BPricingTierRepository. 
Implement business logic for account creation with validation. 
Add pricing calculation based on quantity and business type. 
Handle exceptions and return appropriate error messages.
```

### Prompt 30: Create B2B Pricing Service
```
Create B2BPricingService to calculate volume-based pricing for B2B customers. 
Implement getB2BPricing method that takes productId, businessAccountId, and quantity. 
Apply pricing tiers based on quantity ranges and business type. 
Return PricingResponse with unit price, discount, and total price. 
Cache pricing calculations using Redis for performance.
```

### Prompt 31: Create Purchase Order Service
```
Create PurchaseOrderService with methods: createPurchaseOrder, approvePurchaseOrder, rejectPurchaseOrder, and getPurchaseOrders. 
Implement approval workflow with status transitions (PENDING -> APPROVED/REJECTED). 
Add validation to ensure only authorized users can approve. 
Send notifications on PO status changes. 
Return PurchaseOrderResponse with order details and status.
```

### Prompt 31.5: Create B2B Quote Generation Service
```
Create QuoteService with methods: generateQuote, getQuotes, convertQuoteToOrder, updateQuote. 
Create quotes with custom pricing and terms. 
Set quote expiration dates. 
Allow quote revisions. 
Return QuoteResponse with line items, pricing, and terms.
```

### Prompt 31.6: Create Recurring Orders Service
```
Create RecurringOrderService with methods: createRecurringOrder, updateRecurringOrder, cancelRecurringOrder, getRecurringOrders. 
Support different recurrence patterns (daily, weekly, monthly, quarterly). 
Automatically generate orders based on schedule. 
Send notifications before each recurring order. 
Return RecurringOrderResponse with schedule and next order date.
```

### Prompt 31.7: Create Order Templates Service
```
Create OrderTemplateService with methods: createTemplate, updateTemplate, deleteTemplate, getTemplates, applyTemplate. 
Save frequently used orders as templates. 
Allow template sharing within business account. 
Apply template to create new orders quickly. 
Return OrderTemplateResponse with template items and settings.
```

### Prompt 32: Create Invoice Service
```
Create InvoiceService for B2B invoicing with methods: generateInvoice, getInvoices, markInvoiceAsPaid, and getOverdueInvoices. 
Generate unique invoice numbers with format INV-YYYYMMDD-XXXX. 
Calculate due dates based on credit terms. 
Track payment status and amounts. 
Generate PDF invoices using a template engine.
```

### Prompt 33: Create B2B REST Controller
```
Create B2BController with endpoints: POST /api/v1/b2b/accounts, GET /api/v1/b2b/accounts/{id}, POST /api/v1/b2b/purchase-orders, GET /api/v1/b2b/pricing/{productId}, and GET /api/v1/b2b/invoices. 
Add @PreAuthorize to ensure only BUSINESS role users can access. 
Use @Valid for request validation. 
Return ApiResponse wrapper for all responses. 
Add proper error handling with @ExceptionHandler.
```

### Prompt 33.5: Create B2B Price List Management Service
```
Create PriceListService with methods: createPriceList, updatePriceList, assignPriceList, getPriceListForAccount. 
Support custom price lists per business account. 
Override default pricing with account-specific prices. 
Manage price list versions and effective dates. 
Return PriceListResponse with products and prices.
```

### Prompt 33.6: Create Bulk Ordering Service
```
Create BulkOrderService with methods: createBulkOrder, validateBulkOrder, processBulkOrderItems. 
Support importing orders from CSV/Excel files. 
Validate bulk order data before processing. 
Create multiple orders from bulk import. 
Return BulkOrderResponse with created orders and errors.
```

## Phase 2.5: Admin Features - Additional

### Prompt 34: Create Admin Product Management
```
Create admin/products.tsx page for managing products. 
List all products with: name, SKU, price, stock, status, actions (Edit, Delete, Activate/Deactivate). 
Add "Create Product" button with product creation form. 
Include bulk actions (activate, deactivate, delete). 
Add product import/export functionality.
```

### Prompt 35: Create Admin Order Management
```
Create admin/orders.tsx page for managing all orders. 
List orders with: order number, customer, total, status, date, actions (View, Update Status, Cancel, Refund). 
Add filters for status, date range, customer, order type. 
Show order details modal with full order information. 
Add bulk status update functionality.
```

### Prompt 36: Create Admin User Management
```
Create admin/users.tsx page for managing users. 
List users with: name, email, type, status, registration date, actions (Edit, Suspend, Delete). 
Add filters for user type, status, registration date. 
Show user details with order history and activity. 
Add user role management.
```

### Prompt 37: Create Admin System Configuration
```
Create admin/settings.tsx page for system configuration. 
Sections: Payment Gateways, Shipping Methods, Tax Settings, Email Templates, System Settings. 
Add configuration forms for each section. 
Save settings to database. 
Add settings validation.
```

### Prompt 38: Create Admin Coupon Management
```
Create admin/coupons.tsx page for managing coupons and promotions. 
List coupons with: code, discount, valid dates, usage, status, actions (Edit, Delete, Activate/Deactivate). 
Add "Create Coupon" button with coupon creation form. 
Support different discount types (percentage, fixed amount, free shipping). 
Add usage tracking and analytics.
```

## Phase 3: Backend Services - Retailer

### Prompt 39: Create Retailer Service Layer
```
Create RetailerService with methods: registerRetailer, approveRetailer, getRetailerDashboard, updateRetailer, and calculateCommission. 
Implement retailer registration with bank account validation. 
Create dashboard data aggregation (total sales, orders, products). 
Calculate commission based on configured rate. 
Handle retailer status changes (PENDING -> ACTIVE -> SUSPENDED).
```

### Prompt 40: Create Retailer Product Management Service
```
Create RetailerProductService for managing retailer products: addProduct, updateProductPrice, updateStock, getRetailerProducts, and deactivateProduct. 
Validate product ownership before updates. 
Update inventory when stock changes. 
Send low stock alerts when threshold is reached. 
Return paginated product lists with filters.
```

### Prompt 41: Create Commission Service
```
Create CommissionService to calculate and track platform commissions from retailer sales. 
Implement calculateCommission method based on order total and commission rate. 
Create commission records for each order item. 
Track total commissions per retailer. 
Generate commission reports for admin dashboard.
```

### Prompt 42: Create Retailer REST Controller
```
Create RetailerController with endpoints: POST /api/v1/retailers/register, GET /api/v1/retailers/dashboard, POST /api/v1/retailers/{id}/products, GET /api/v1/retailers/{id}/products, and GET /api/v1/retailers/{id}/orders. 
Add authentication and authorization checks. 
Validate retailer ownership for update operations. 
Return dashboard data with sales charts and statistics. 
Include pagination for list endpoints.
```

### Prompt 42.5: Create Retailer Storefront Service
```
Create RetailerStorefrontService with methods: getStorefront, updateStorefront, getStorefrontProducts, searchRetailers. 
Create public-facing retailer store pages. 
Display retailer information, products, and reviews. 
Support retailer search and filtering. 
Return StorefrontResponse with retailer details and product listings.
```

### Prompt 42.6: Create Bulk Product Import Service
```
Create BulkImportService with methods: importProducts, validateImportFile, getImportTemplate, getImportHistory. 
Support CSV/Excel file imports for products. 
Validate product data before import. 
Handle import errors and provide detailed reports. 
Return ImportResponse with success count and error details.
```

### Prompt 42.7: Create Retailer Payout Management Service
```
Create PayoutService with methods: calculatePayout, processPayout, getPayoutHistory, getPendingPayouts. 
Calculate retailer earnings (sales - commission). 
Process payouts to retailer bank accounts. 
Support scheduled payouts (weekly, monthly). 
Return PayoutResponse with amount and status.
```

## Phase 3.5: Shipping & Returns

### Prompt 43: Create Shipping Service
```
Create ShippingService with methods: calculateShipping, getShippingMethods, trackShipment, createShipmentLabel. 
Integrate with shipping carriers (FedEx, UPS, DHL APIs). 
Calculate shipping costs based on weight, dimensions, and destination. 
Generate shipping labels. 
Return ShippingResponse with cost and estimated delivery.
```

### Prompt 44: Create Return/Refund Service
```
Create ReturnService with methods: createReturnRequest, approveReturn, rejectReturn, processRefund, getReturnHistory. 
Implement return request workflow. 
Validate return eligibility (time window, item condition). 
Process refunds through payment gateway. 
Return ReturnResponse with status and refund details.
```

### Prompt 44.5: Create Automatic Reordering Service
```
Create AutoReorderService with methods: setupAutoReorder, updateReorderSettings, processAutoReorder, getReorderSettings. 
Monitor inventory levels against reorder points. 
Automatically create purchase orders when stock is low. 
Support different reorder strategies (fixed quantity, economic order quantity). 
Send notifications when auto-reorders are triggered.
```

### Prompt 45: Create Return Management Pages
```
Create returns/index.tsx page for customers to request returns. 
Create admin/returns.tsx page for managing return requests. 
Show return request form with reason and item selection. 
Display return status and tracking. 
Add return label generation.
```

### Prompt 45.5: Create Low Stock Alerts Service
```
Create LowStockAlertService with methods: checkLowStock, sendLowStockAlerts, getLowStockProducts, updateAlertThresholds. 
Monitor inventory levels against thresholds. 
Send alerts to retailers and admins when stock is low. 
Support different alert levels (warning, critical). 
Return LowStockAlertResponse with products and stock levels.
```

### Prompt 45.6: Create Stock Adjustment Service
```
Create StockAdjustmentService with methods: adjustStock, getAdjustmentHistory, approveAdjustment, rejectAdjustment. 
Support manual stock adjustments with reasons. 
Require approval for large adjustments. 
Track adjustment history and audit trail. 
Return StockAdjustmentResponse with adjustment details.
```

## Phase 4: Enhanced Order Service

### Prompt 46: Enhance Order Service for Multi-Type Orders
```
Enhance existing OrderService to support B2C, B2B, and Retailer order types. 
Add createOrder method that accepts orderType parameter. 
Implement different workflows: B2C (immediate payment), B2B (PO approval), Retailer (split payment). 
Calculate shipping based on order type and addresses. 
Update order status based on type-specific business rules.
```

### Prompt 47: Create Multi-Vendor Order Processing
```
Create MultiVendorOrderService to handle marketplace orders with items from multiple retailers. 
Split single order into retailer-specific sub-orders. 
Calculate shipping per retailer. 
Process payments: split total amount across retailers, deduct commission. 
Create separate shipments for each retailer's items.
```

### Prompt 48: Create Order Status Workflow Service
```
Create OrderStatusService to manage order status transitions for different order types. 
B2C: PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED. 
B2B: PENDING -> PO_APPROVED -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED. 
Retailer: PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED. 
Send status update notifications. 
Log all status changes for audit trail.
```

## Phase 4.5: Search & Recommendations

### Prompt 49: Create Search Service (Elasticsearch)
```
Create SearchService with Elasticsearch integration for product search. 
Implement faceted search (category, price range, brand, rating). 
Add search suggestions and autocomplete. 
Support advanced search filters. 
Return SearchResponse with products and facets.
```

### Prompt 50: Create Product Recommendation Service
```
Create RecommendationService with methods: getRecommendedProducts, getSimilarProducts, getRecentlyViewed. 
Implement recommendation algorithms (collaborative filtering, content-based). 
Track user behavior and preferences. 
Return ProductResponse list with recommendations.
```

### Prompt 51: Create Search Frontend
```
Create search page with advanced filters and faceted search UI. 
Add search autocomplete component. 
Show search suggestions. 
Display search results with filters sidebar. 
Add search analytics tracking.
```

## Phase 4.6: Promotions & Marketing

### Prompt 51.5: Create Flash Sales Service
```
Create FlashSaleService with methods: createFlashSale, updateFlashSale, getActiveFlashSales, applyFlashSaleDiscount. 
Set flash sale start and end times. 
Limit quantity available at sale price. 
Track flash sale performance. 
Return FlashSaleResponse with discount and availability.
```

### Prompt 51.6: Create Loyalty Program Service
```
Create LoyaltyService with methods: enrollInLoyalty, earnPoints, redeemPoints, getLoyaltyStatus, getLoyaltyHistory. 
Track points earned from purchases. 
Define point redemption rules. 
Support different loyalty tiers. 
Return LoyaltyResponse with points balance and tier information.
```

### Prompt 51.7: Create Referral Program Service
```
Create ReferralService with methods: generateReferralCode, trackReferral, processReferralReward, getReferralStats. 
Generate unique referral codes for users. 
Track referrals and conversions. 
Reward both referrer and referee. 
Return ReferralResponse with code and reward details.
```

### Prompt 51.8: Create Email Marketing Integration Service
```
Create EmailMarketingService to integrate with email marketing platforms (Mailchimp, SendGrid, Constant Contact). 
Sync customer segments and lists. 
Track email campaign performance. 
Support automated email campaigns. 
Return EmailMarketingResponse with sync status and campaign metrics.
```

## Phase 5: Frontend - B2B Features

### Prompt 52: Create B2B Type Definitions
```
Create TypeScript type definitions in vendo/vendo-frontend/src/types/b2b.ts for BusinessAccount, B2BPricing, PurchaseOrder, Invoice, and BusinessContact. 
Add request types: BusinessAccountRequest, PurchaseOrderRequest. 
Include response types with all fields from backend. 
Export all types for use in components and services. 
Add JSDoc comments for complex types.
```

### Prompt 53: Create B2B API Service
```
Create b2b-api.ts in vendo/vendo-frontend/src/services/ with methods: createAccount, getAccount, getPricing, createPurchaseOrder, getPurchaseOrders, getInvoices, and markInvoicePaid. 
Use apiClient from @/lib/api. 
Handle errors with try-catch and show toast notifications. 
Return typed responses using B2B types. 
Add loading states for async operations.
```

### Prompt 54: Create B2B Store (Zustand)
```
Create b2b-store.ts in vendo/vendo-frontend/src/stores/ using Zustand for B2B state management. 
Store: currentBusinessAccount, purchaseOrders, invoices, pricingCache. 
Actions: setBusinessAccount, loadPurchaseOrders, loadInvoices, getPricing. 
Persist business account to localStorage. 
Add loading and error states.
```

### Prompt 55: Create B2B Registration Page
```
Create b2b/register.tsx page in vendo/vendo-frontend/src/pages/ with form for business account registration. 
Fields: companyName, businessRegistrationNumber, taxId, businessType, creditLimit, billingAddress. 
Use shadcn/ui Form, Input, Select, and Button components. 
Validate all fields before submission. 
Redirect to B2B dashboard on success.
```

### Prompt 56: Create B2B Dashboard Page
```
Create b2b/dashboard.tsx page showing business account overview. 
Display: account status, credit limit, available credit, recent purchase orders, pending invoices, and order statistics. 
Use Card components for each section. 
Add charts using recharts library for sales trends. 
Fetch data using TanStack Query with useQuery hook.
```

### Prompt 57: Create B2B Product Catalog Page
```
Create b2b/products.tsx page showing products with B2B volume pricing. 
Display quantity input and show price tiers (e.g., 1-10: $100, 11-50: $90, 51+: $80). 
Add "Add to Cart" button that uses B2B pricing. 
Show product details with bulk pricing information. 
Use ProductGrid component with B2B pricing overlay.
```

### Prompt 58: Create Purchase Order Management Page
```
Create b2b/purchase-orders.tsx page for managing purchase orders. 
List all POs with status, date, total amount, and approval status. 
Add "Create PO" button that opens modal with order selection. 
Show PO details with items and approval workflow. 
Add filters for status (PENDING, APPROVED, REJECTED).
```

### Prompt 59: Create Invoice Management Page
```
Create b2b/invoices.tsx page for viewing and managing invoices. 
Display invoice list with: invoice number, issue date, due date, amount, status. 
Add filters for status (PENDING, PAID, OVERDUE). 
Show invoice details with line items and payment history. 
Add "Mark as Paid" button for manual payment entry.
```

## Phase 5.5: Mobile App - B2C Features

### Prompt 60: Create B2C Mobile Screens
```
Create B2C screens in vendo/vendo-mobile/app/: home.tsx, products.tsx, product/[id].tsx, cart.tsx, checkout.tsx, profile.tsx, orders.tsx. 
Use React Native components with Expo Router. 
Fetch data using TanStack Query. 
Add navigation between screens. 
Style with consistent design system.
```

### Prompt 61: Create Mobile Push Notifications
```
Implement push notifications in mobile app for: order updates, promotions, low stock alerts, new products. 
Configure Expo Push Notification service. 
Handle notification permissions. 
Create notification handlers. 
Add notification preferences in settings.
```

## Phase 6: Frontend - Retailer Features

### Prompt 62: Create Retailer Type Definitions
```
Create TypeScript types in vendo/vendo-frontend/src/types/retailer.ts for Retailer, RetailerProduct, RetailerDashboard, Commission, and RetailerOrder. 
Add request types: RetailerRegistrationRequest, ProductListingRequest. 
Include response types matching backend API. 
Export all types. 
Add JSDoc comments.
```

### Prompt 63: Create Retailer API Service
```
Create retailer-api.ts in vendo/vendo-frontend/src/services/ with methods: registerRetailer, getDashboard, addProduct, updateProduct, getProducts, getOrders, and getAnalytics. 
Use apiClient with proper error handling. 
Show toast notifications for success/error. 
Return typed responses. 
Handle file uploads for product images.
```

### Prompt 64: Create Retailer Store (Zustand)
```
Create retailer-store.ts in vendo/vendo-frontend/src/stores/ for retailer state management. 
Store: currentRetailer, products, orders, dashboardData, analytics. 
Actions: setRetailer, loadProducts, loadOrders, loadDashboard, updateProduct. 
Persist retailer info to localStorage. 
Add loading states for all async operations.
```

### Prompt 65: Create Retailer Registration Page
```
Create retailer/register.tsx page with retailer registration form. 
Fields: storeName, storeSlug, description, commissionRate, bankAccount details, tax information. 
Add image upload for store logo and banner. 
Validate store slug uniqueness. 
Redirect to retailer dashboard on approval.
```

### Prompt 66: Create Retailer Dashboard Page
```
Create retailer/dashboard.tsx with comprehensive retailer overview. 
Display: total sales, total orders, pending orders, active products, low stock alerts, recent orders table, and sales chart. 
Use Card components for metrics. 
Add quick actions: Add Product, View Orders, View Analytics. 
Fetch real-time data using TanStack Query.
```

### Prompt 67: Create Retailer Product Management Page
```
Create retailer/products.tsx page for managing retailer products. 
List all products with: name, price, stock, status, actions (Edit, Deactivate). 
Add "Add Product" button that opens product selection modal. 
Include search and filter functionality. 
Show low stock warnings with red badges.
```

### Prompt 68: Create Retailer Order Management Page
```
Create retailer/orders.tsx page for managing retailer orders. 
Display order list with: order number, customer, items, total, status, date. 
Add status filters (PENDING, CONFIRMED, SHIPPED, DELIVERED). 
Show order details modal with customer info and items. 
Add "Update Status" button for order processing.
```

### Prompt 69: Create Retailer Analytics Page
```
Create retailer/analytics.tsx page with sales analytics and reports. 
Display: sales over time chart, top products, revenue by category, customer demographics. 
Add date range picker for custom reports. 
Show commission breakdown and payout history. 
Export reports to CSV/PDF.
```

## Phase 6.5: Analytics & Reporting

### Prompt 70: Create Analytics Service
```
Create AnalyticsService to track: page views, product views, cart additions, purchases, user behavior. 
Integrate with analytics platforms (Google Analytics, Mixpanel). 
Track custom events. 
Generate analytics reports. 
Return AnalyticsResponse with metrics.
```

### Prompt 71: Create Reporting Service
```
Create ReportingService with methods: generateSalesReport, generateProductReport, generateCustomerReport, exportReport. 
Support date range filtering. 
Generate reports in multiple formats (PDF, CSV, Excel). 
Schedule automated reports. 
Return ReportResponse with data and download link.
```

### Prompt 72: Create Analytics Dashboard
```
Create analytics/dashboard.tsx page with charts and metrics. 
Display: sales over time, top products, top customers, revenue by category, conversion rates. 
Add date range picker. 
Export reports functionality. 
Use recharts or similar library for visualizations.
```

## Phase 7: Enhanced Cart & Checkout

### Prompt 73: Enhance Cart Service for Multi-Type
```
Enhance existing cart service to support B2C, B2B, and multi-vendor carts. 
Add cartType field to Cart entity. 
Implement getCartPricing that applies B2B pricing for business accounts. 
Handle multi-vendor carts with items from different retailers. 
Calculate shipping per retailer for marketplace orders.
```

### Prompt 74: Create Enhanced Checkout Page
```
Enhance checkout.tsx page to support B2C, B2B, and multi-vendor checkout. 
Show different flows: B2C (immediate payment), B2B (PO creation), Retailer (split payment). 
Display shipping options per retailer for marketplace. 
Add purchase order notes field for B2B. 
Show order summary with commission breakdown for marketplace.
```

### Prompt 75: Create B2B Checkout Component
```
Create B2BCheckout component for business account checkout flow. 
Show credit limit and available credit. 
Display purchase order approval workflow. 
Add multiple shipping addresses selection. 
Show volume pricing summary. 
Submit creates PO instead of immediate order.
```

## Phase 7.5: File Upload & Media Management

### Prompt 76: Create File Upload Service (Frontend)
```
Create file upload components in vendo/vendo-frontend/src/components/upload/. 
Support image uploads for products, user avatars, store logos. 
Add drag-and-drop functionality. 
Show upload progress. 
Handle file validation and error states.
```

### Prompt 77: Create Media Management Service
```
Create MediaService with methods: uploadFile, deleteFile, getFileUrl, resizeImage, optimizeImage. 
Integrate with S3 or MinIO for file storage. 
Handle image resizing and optimization. 
Return MediaResponse with file URL and metadata.
```

## Phase 7.6: Security & Advanced Features

### Prompt 77.5: Create Two-Factor Authentication Service
```
Create TwoFactorAuthService with methods: enable2FA, disable2FA, verify2FACode, generateBackupCodes. 
Support TOTP (Time-based One-Time Password) authentication. 
Generate QR codes for authenticator apps. 
Provide backup codes for account recovery. 
Return 2FAResponse with setup instructions.
```

### Prompt 77.6: Create CDN Integration Service
```
Integrate CDN (Content Delivery Network) for static assets. 
Configure CDN for images, CSS, and JavaScript files. 
Implement cache invalidation strategies. 
Monitor CDN performance and costs. 
Return CDN URLs for optimized asset delivery.
```

### Prompt 77.7: Create Image Optimization Service
```
Create ImageOptimizationService to automatically optimize uploaded images. 
Resize images to multiple sizes (thumbnail, medium, large). 
Convert images to WebP format for better compression. 
Generate responsive image sets. 
Return optimized image URLs with different sizes.
```

## Phase 8: Admin Features

### Prompt 78: Create Admin Business Account Management
```
Create admin/business-accounts.tsx page for managing business accounts. 
List all business accounts with: company name, status, credit limit, total orders. 
Add filters for status (PENDING, APPROVED, SUSPENDED). 
Show account details with approval/rejection actions. 
Add credit limit adjustment functionality.
```

### Prompt 79: Create Admin Retailer Management
```
Create admin/retailers.tsx page for managing retailers. 
List all retailers with: store name, status, total sales, commission rate. 
Add filters and search functionality. 
Show retailer details with approval actions. 
Add commission rate adjustment and payout management.
```

### Prompt 80: Create Admin Dashboard
```
Create admin/dashboard.tsx with comprehensive admin overview. 
Display: total sales, total orders, active users, active retailers, pending approvals. 
Show charts: sales over time, revenue by type (B2C/B2B/Retailer), top products. 
Add quick actions: Approve Business Accounts, Approve Retailers, View Reports. 
Include recent activity feed.
```

## Phase 9: Mobile App Features

### Prompt 81: Create B2B Mobile Screens
```
Create B2B screens in vendo/vendo-mobile/app/b2b/: index.tsx (dashboard), products.tsx (catalog), orders.tsx (POs), invoices.tsx. 
Use React Native components with Expo Router. 
Fetch data using TanStack Query. 
Add navigation between screens. 
Style with consistent design system.
```

### Prompt 82: Create Retailer Mobile Screens
```
Create Retailer screens in vendo/vendo-mobile/app/retailer/: dashboard.tsx, products.tsx, orders.tsx, analytics.tsx. 
Display retailer dashboard with key metrics. 
Add product management with image upload. 
Show order list with status updates. 
Include basic analytics charts.
```

## Phase 10: Testing & Quality

### Prompt 83: Create B2B Service Unit Tests
```
Create unit tests for B2BService in vendo-backend/src/test/java/com/vendo/b2b/service/. 
Test: createBusinessAccount, approveBusinessAccount, getB2BPricing, calculateVolumeDiscount. 
Use Mockito for mocking dependencies. 
Test edge cases: invalid data, insufficient credit, pricing tier boundaries. 
Achieve 80%+ code coverage.
```

### Prompt 84: Create Retailer Service Unit Tests
```
Create unit tests for RetailerService covering: registerRetailer, approveRetailer, calculateCommission, getDashboard. 
Mock all repository dependencies. 
Test commission calculation with different rates. 
Test dashboard data aggregation. 
Verify error handling for invalid inputs.
```

### Prompt 85: Create Frontend Component Tests
```
Create React Testing Library tests for B2B and Retailer components. 
Test: B2BRegistrationForm, RetailerDashboard, ProductManagement. 
Mock API calls using MSW (Mock Service Worker). 
Test user interactions and form validation. 
Test error states and loading states.
```

## Phase 11: Performance & Optimization

### Prompt 86: Implement Redis Caching
```
Implement Redis caching in backend services for: product listings, B2B pricing, retailer dashboard data. 
Add @Cacheable annotations to service methods. 
Configure cache TTL: products (1 hour), pricing (30 min), dashboard (5 min). 
Add cache eviction on data updates. 
Monitor cache hit rates.
```

### Prompt 87: Optimize Database Queries
```
Optimize database queries in repositories using: @Query with JOIN FETCH, pagination, and proper indexes. 
Add database indexes on frequently queried columns. 
Use projection DTOs to reduce data transfer. 
Implement query result caching. 
Add query performance monitoring.
```

### Prompt 88: Implement API Rate Limiting
```
Implement rate limiting for API endpoints using Spring Security and Redis. 
Set limits: authenticated users (1000/hour), unauthenticated (100/hour). 
Add rate limit headers to responses. 
Create custom exception for rate limit exceeded. 
Add rate limit configuration in application.yml.
```

## Phase 11.5: External Integrations

### Prompt 88.5: Create Accounting Software Integration
```
Create AccountingIntegrationService to sync orders, invoices, and payments with accounting software (QuickBooks, Xero, Sage). 
Implement webhook handlers for accounting system events. 
Map e-commerce data to accounting formats. 
Handle two-way synchronization. 
Return IntegrationResponse with sync status.
```

### Prompt 88.6: Create CRM Integration Service
```
Create CRMIntegrationService to sync customer data with CRM systems (Salesforce, HubSpot). 
Export customer information and order history. 
Import CRM contacts as customers. 
Track customer interactions and lifecycle. 
Return CRMIntegrationResponse with sync details.
```

### Prompt 88.7: Create ERP Integration Service
```
Create ERPIntegrationService to integrate with ERP systems (SAP, Oracle, Microsoft Dynamics). 
Sync inventory levels and product data. 
Export orders and shipments to ERP. 
Import pricing and product information. 
Return ERPIntegrationResponse with integration status.
```

## Phase 12: Documentation & Deployment

### Prompt 89: Create API Documentation with Swagger
```
Enhance Swagger/OpenAPI documentation for all new B2B and Retailer endpoints. 
Add @Operation, @ApiResponse, and @Parameter annotations. 
Include request/response examples. 
Document authentication requirements. 
Generate interactive API docs accessible at /swagger-ui.html.
```

### Prompt 90: Create Deployment Configuration
```
Create Docker configuration: Dockerfile for backend, frontend Dockerfile, docker-compose.yml for local development. 
Add environment variable configuration files (.env.example). 
Create Kubernetes manifests for production deployment. 
Add CI/CD pipeline configuration (GitHub Actions). 
Include health check endpoints.
```

---

## How to Use These Prompts

1. **Start with Phase 0** - Core B2C database, entities, and services
2. **Continue with Phase 0.5-0.6** - Core B2C backend and frontend
3. **Move to Phase 1** - Additional database tables (B2B, Retailer, Orders, Inventory)
4. **Continue with Phase 1.5** - Payment and notification integration
5. **Move to Phase 2-4** - Backend services (B2B, Retailer, Orders)
6. **Add Phase 2.5** - Additional admin features
7. **Add Phase 3.5** - Shipping and returns
8. **Add Phase 4.5** - Search and recommendations
9. **Continue with Phase 5-7** - Frontend features (B2B, Retailer, Cart/Checkout)
10. **Add Phase 5.5** - Mobile B2C features
11. **Add Phase 6.5** - Analytics and reporting
12. **Add Phase 7.5** - File upload and media management
13. **Complete Phase 8-9** - Admin and mobile (B2B/Retailer)
14. **Finish with Phase 10-12** - Testing, optimization, and deployment

**Copy one prompt at a time** and paste it when you're ready to implement that specific feature. Each prompt is self-contained and will guide the implementation of that component.

---

## Summary

**Total Prompts: 120+ (including sub-prompts)**

The prompts are organized in logical phases from core B2C functionality through advanced features and integrations. Each phase builds upon previous phases to create a complete end-to-end e-commerce platform.

### Coverage Summary:
- ✅ **Core B2C Features**: Database, entities, services, frontend (18 prompts)
- ✅ **Content Management**: Blog, pages, media (2 prompts)
- ✅ **B2B Features**: Database, services, frontend, quotes, recurring orders, templates (18 prompts)
- ✅ **Retailer/Marketplace**: Database, services, frontend, storefronts, payouts (13 prompts)
- ✅ **Order Management**: Multi-type orders, workflows, cancellation (9 prompts)
- ✅ **Payment & Notifications**: Gateway integration, templates, preferences (7 prompts)
- ✅ **Shipping & Returns**: Carrier integration, return management, auto-reorder (4 prompts)
- ✅ **Search & Recommendations**: Elasticsearch, recommendations (3 prompts)
- ✅ **Promotions & Marketing**: Flash sales, loyalty, referrals, email marketing (4 prompts)
- ✅ **Inventory Management**: Multi-warehouse, stock alerts, adjustments (4 prompts)
- ✅ **Tax & Shipping**: Tax calculation, shipping method selection (2 prompts)
- ✅ **Admin Features**: Product, order, user management, settings, coupons (8 prompts)
- ✅ **Mobile Apps**: B2C, B2B, Retailer screens (4 prompts)
- ✅ **Analytics & Reporting**: Services and dashboards (3 prompts)
- ✅ **File Upload & Media**: Frontend and backend services (2 prompts)
- ✅ **Security & Performance**: 2FA, CDN, image optimization (3 prompts)
- ✅ **External Integrations**: Accounting, CRM, ERP (3 prompts)
- ✅ **Testing & Deployment**: Unit tests, optimization, deployment (6 prompts)

**Total: 90 main prompts + 30+ sub-prompts = 120+ prompts**

All critical features for a complete end-to-end e-commerce platform are now covered!
