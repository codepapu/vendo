# Entity Analysis: DATABASE_SCHEMA.md vs Existing Entities

## Summary
This document compares all tables defined in DATABASE_SCHEMA.md with existing JPA entities in the codebase.

## ✅ Existing Entities (Matching Schema)

### 1. Users & Authentication
- ✅ **User** (`users`) - EXISTS and matches schema
- ✅ **Address** (`addresses`) - EXISTS and matches schema

### 2. Products & Catalog
- ✅ **Product** (`products`) - EXISTS, updated to match schema
- ✅ **Category** (`categories`) - EXISTS, updated to match schema
- ✅ **ProductAttribute** (`product_attributes`) - EXISTS, updated to match schema
- ✅ **ProductVariation** (`product_variations`) - EXISTS, updated to match schema
- ✅ **ProductVariationItem** (`product_variation_items`) - EXISTS, updated to match schema
- ✅ **Wishlist** (`wishlists`) - EXISTS, updated to match schema
- ✅ **ProductReview** (`product_reviews`) - EXISTS, updated to match schema
- ✅ **Coupon** (`coupons`) - EXISTS and matches schema

### 3. Orders & Transactions
- ⚠️ **Order** (`orders`) - EXISTS but uses `ec_orders` table name, needs update to match schema
- ⚠️ **OrderProduct** (`order_items`) - EXISTS but uses `ec_order_products` table name, needs update
- ✅ **Invoice** (`invoices`) - EXISTS but uses `ec_invoices` table name, needs update
- ✅ **Payment** (`payments`) - EXISTS

### 4. Cart & Checkout
- ⚠️ **Cart** (`carts`) - EXISTS but uses different structure (`ec_cart`), needs update
- ❌ **CartItem** (`cart_items`) - MISSING - needs to be created

### 5. Addresses & Shipping
- ✅ **Address** (`addresses`) - EXISTS
- ✅ **ShippingMethod** (`shipping_methods`) - EXISTS
- ⚠️ **Shipment** (`shipments`) - EXISTS but uses `ec_shipments` table name, needs update

## ❌ Missing Entities (Need to be Created)

### 1. B2B Entities
- ❌ **BusinessAccount** (`business_accounts`) - MISSING
- ❌ **BusinessContact** (`business_contacts`) - MISSING

### 2. Retailer/Marketplace Entities
- ❌ **Retailer** (`retailers`) - MISSING
- ❌ **RetailerBankAccount** (`retailer_bank_accounts`) - MISSING
- ❌ **ProductRetailer** (`product_retailers`) - MISSING
- ❌ **RetailerReview** (`retailer_reviews`) - MISSING

### 3. B2B Pricing
- ❌ **B2BPricingTier** (`b2b_pricing_tiers`) - MISSING
- ❌ **VolumeDiscount** (`volume_discounts`) - MISSING

### 4. Orders
- ❌ **PurchaseOrder** (`purchase_orders`) - MISSING
- ⚠️ **OrderItem** (`order_items`) - EXISTS as `OrderProduct` but needs to match schema exactly

### 5. Inventory Management
- ❌ **Warehouse** (`warehouses`) - MISSING
- ❌ **Inventory** (`inventory`) - MISSING
- ❌ **StockMovement** (`stock_movements`) - MISSING

### 6. Cart
- ❌ **CartItem** (`cart_items`) - MISSING

## ⚠️ Entities Needing Updates

### 1. Order Entity
**Current:** Uses `ec_orders` table  
**Required:** Should use `orders` table with fields:
- `order_number` (VARCHAR(50) UNIQUE) - currently `code`
- `business_account_id` - MISSING
- `order_type` (B2C, B2B, RETAILER) - MISSING
- `payment_status` - MISSING
- `subtotal`, `tax_amount`, `shipping_amount`, `discount_amount`, `total_amount` - needs verification
- `currency` - MISSING
- `notes` - exists as `description`

### 2. OrderItem Entity (OrderProduct)
**Current:** Uses `ec_order_products` table  
**Required:** Should use `order_items` table with fields:
- `retailer_id` - MISSING (for marketplace orders)
- `product_name`, `sku`, `quantity`, `unit_price`, `total_price` - exists but field names differ
- `commission_amount` - MISSING (for marketplace orders)

### 3. Cart Entity
**Current:** Uses `ec_cart` with composite key (identifier, instance)  
**Required:** Should use `carts` table with:
- `user_id` - MISSING
- `business_account_id` - MISSING
- `session_id` - MISSING (for guest carts)
- `cart_type` (B2C, B2B) - MISSING
- `currency` - MISSING

### 4. Shipment Entity
**Current:** Uses `ec_shipments` table  
**Required:** Should use `shipments` table with:
- `shipping_method_id` - needs verification
- `tracking_number`, `carrier`, `status` - exists
- `shipped_at`, `delivered_at` - exists but field names may differ

### 5. Invoice Entity
**Current:** Uses `ec_invoices` table  
**Required:** Should use `invoices` table with:
- `invoice_number` (VARCHAR(50) UNIQUE) - needs verification
- `order_id`, `business_account_id` - needs verification
- `issue_date`, `due_date` - needs verification
- `status`, `subtotal`, `tax_amount`, `total_amount`, `paid_amount`, `paid_at` - needs verification

## 📋 Priority List for Implementation

### Phase 1: Core Missing Entities (High Priority)
1. **BusinessAccount** - Required for B2B functionality
2. **BusinessContact** - Required for B2B functionality
3. **Retailer** - Required for marketplace functionality
4. **RetailerBankAccount** - Required for marketplace payouts
5. **ProductRetailer** - Required for marketplace product ownership
6. **CartItem** - Required for cart functionality

### Phase 2: B2B & Pricing Entities
7. **B2BPricingTier** - Required for B2B volume pricing
8. **PurchaseOrder** - Required for B2B order workflow
9. **VolumeDiscount** - Required for volume-based discounts

### Phase 3: Inventory Management
10. **Warehouse** - Required for multi-warehouse inventory
11. **Inventory** - Required for stock management
12. **StockMovement** - Required for inventory tracking

### Phase 4: Reviews & Enhancements
13. **RetailerReview** - Required for retailer ratings

### Phase 5: Entity Updates
14. Update **Order** entity to match schema
15. Update **OrderItem** (OrderProduct) to match schema
16. Update **Cart** entity to match schema
17. Update **Shipment** entity to match schema
18. Update **Invoice** entity to match schema

## 🔧 Enhancement Recommendations

### 1. Table Name Consistency
Many entities use `ec_` prefix (e.g., `ec_orders`, `ec_invoices`). Consider:
- Option A: Update all entities to match DATABASE_SCHEMA.md (remove `ec_` prefix)
- Option B: Update DATABASE_SCHEMA.md to reflect current naming convention
- **Recommendation:** Update entities to match DATABASE_SCHEMA.md for consistency

### 2. Field Name Mapping
Some entities have different field names:
- `Order.code` → should be `Order.orderNumber`
- `OrderProduct` → should be `OrderItem`
- Field names need to match schema exactly

### 3. Missing Relationships
Several entities need additional relationships:
- `Order.businessAccount` - ManyToOne to BusinessAccount
- `OrderItem.retailer` - ManyToOne to Retailer
- `Cart.user` and `Cart.businessAccount` - ManyToOne relationships
- `ProductRetailer` - ManyToOne to Product and Retailer

### 4. Indexes and Constraints
Verify all indexes and unique constraints match DATABASE_SCHEMA.md:
- Unique constraints on `order_number`, `invoice_number`, `po_number`
- Indexes on foreign keys and frequently queried fields

## 📊 Statistics

- **Total Tables in Schema:** 31
- **Existing Entities:** 15
- **Missing Entities:** 13 ✅ **ALL CREATED**
- **Entities Needing Updates:** 5 ✅ **ALL UPDATED**
- **Completion Rate:** 100% ✅ **ALL ENTITIES COMPLETE**

## ✅ Implementation Status

### All Missing Entities Created:
1. ✅ BusinessAccount - Created in `com.vendo.b2b.entity`
2. ✅ BusinessContact - Created in `com.vendo.b2b.entity`
3. ✅ Retailer - Created in `com.vendo.retailer.entity`
4. ✅ RetailerBankAccount - Created in `com.vendo.retailer.entity`
5. ✅ ProductRetailer - Created in `com.vendo.product.entity`
6. ✅ B2BPricingTier - Created in `com.vendo.product.entity`
7. ✅ PurchaseOrder - Created in `com.vendo.order.entity`
8. ✅ RetailerReview - Created in `com.vendo.product.entity`
9. ✅ VolumeDiscount - Created in `com.vendo.product.entity`
10. ✅ Warehouse - Created in `com.vendo.inventory.entity`
11. ✅ Inventory - Created in `com.vendo.inventory.entity`
12. ✅ StockMovement - Created in `com.vendo.inventory.entity`
13. ✅ CartItem - Created in `com.vendo.cart.entity`

### All Entities Updated:
1. ✅ Order - Updated to match schema (table: `orders`, fields: order_number, business_account_id, order_type, payment_status, currency, etc.)
2. ✅ OrderItem - Created new entity matching schema (table: `order_items` with retailer_id, commission_amount)
3. ✅ Cart - Updated to match schema (table: `carts` with user_id, business_account_id, session_id, cart_type, currency)
4. ✅ Shipment - Updated to match schema (table: `shipments` with shipping_method_id, tracking_number, carrier, shipped_at, delivered_at)
5. ✅ Invoice - Updated to match schema (table: `invoices` with invoice_number, order_id, business_account_id, issue_date, due_date, paid_amount, paid_at)
6. ✅ Payment - Updated to match schema (fields: invoice_id, payment_method, payment_gateway, transaction_id, gateway_response)

## 📝 Detailed Missing Entity Specifications

### 1. BusinessAccount Entity
**Table:** `business_accounts`  
**Package:** `com.vendo.b2b.entity`  
**Required Fields:**
- id, user_id, company_name, business_registration_number, tax_id
- business_type (CORPORATE, SME, ENTERPRISE)
- credit_limit, credit_terms_days, payment_terms
- billing_address_id, status, approved_at, approved_by
- created_at, updated_at

**Relationships:**
- @ManyToOne to User
- @OneToMany to BusinessContact
- @OneToMany to PurchaseOrder
- @OneToMany to Invoice
- @OneToMany to Order
- @OneToMany to Address

### 2. BusinessContact Entity
**Table:** `business_contacts`  
**Package:** `com.vendo.b2b.entity`  
**Required Fields:**
- id, business_account_id, user_id
- contact_type (PRIMARY, BILLING, SHIPPING, PURCHASER)
- is_primary, created_at

**Relationships:**
- @ManyToOne to BusinessAccount
- @ManyToOne to User

### 3. Retailer Entity
**Table:** `retailers`  
**Package:** `com.vendo.marketplace.entity` or `com.vendo.retailer.entity`  
**Required Fields:**
- id, user_id, store_name, store_slug (UNIQUE)
- description, logo_url, banner_url
- commission_rate, status, rating
- total_sales, total_orders, verified
- created_at, updated_at

**Relationships:**
- @ManyToOne to User
- @OneToMany to RetailerBankAccount
- @OneToMany to ProductRetailer
- @OneToMany to RetailerReview
- @OneToMany to OrderItem (for marketplace orders)

### 4. RetailerBankAccount Entity
**Table:** `retailer_bank_accounts`  
**Package:** `com.vendo.marketplace.entity` or `com.vendo.retailer.entity`  
**Required Fields:**
- id, retailer_id
- account_holder_name, account_number, bank_name
- ifsc_code, swift_code, is_primary
- created_at

**Relationships:**
- @ManyToOne to Retailer

### 5. ProductRetailer Entity
**Table:** `product_retailers`  
**Package:** `com.vendo.product.entity`  
**Required Fields:**
- id, product_id, retailer_id (UNIQUE constraint)
- price, compare_at_price, cost_price
- stock_quantity, low_stock_threshold, is_active
- created_at, updated_at

**Relationships:**
- @ManyToOne to Product
- @ManyToOne to Retailer

### 6. B2BPricingTier Entity
**Table:** `b2b_pricing_tiers`  
**Package:** `com.vendo.product.entity`  
**Required Fields:**
- id, product_id, business_type
- min_quantity, max_quantity
- price, discount_percentage, is_active
- created_at

**Relationships:**
- @ManyToOne to Product

### 7. PurchaseOrder Entity
**Table:** `purchase_orders`  
**Package:** `com.vendo.order.entity`  
**Required Fields:**
- id, po_number (UNIQUE), business_account_id
- order_id, status (PENDING, APPROVED, REJECTED, COMPLETED)
- requested_by, approved_by, approval_notes
- requested_at, approved_at, created_at

**Relationships:**
- @ManyToOne to BusinessAccount
- @ManyToOne to Order
- @ManyToOne to User (requested_by, approved_by)

### 8. RetailerReview Entity
**Table:** `retailer_reviews`  
**Package:** `com.vendo.product.entity` or `com.vendo.marketplace.entity`  
**Required Fields:**
- id, retailer_id, user_id, order_id
- rating (1-5), review_text
- is_approved, created_at

**Relationships:**
- @ManyToOne to Retailer
- @ManyToOne to User
- @ManyToOne to Order

### 9. VolumeDiscount Entity
**Table:** `volume_discounts`  
**Package:** `com.vendo.product.entity`  
**Required Fields:**
- id, product_id, min_quantity
- discount_percentage, is_active
- valid_from, valid_until, created_at

**Relationships:**
- @ManyToOne to Product

### 10. Warehouse Entity
**Table:** `warehouses`  
**Package:** `com.vendo.inventory.entity`  
**Required Fields:**
- id, name, code (UNIQUE)
- address_id, is_active, created_at

**Relationships:**
- @ManyToOne to Address
- @OneToMany to Inventory

### 11. Inventory Entity
**Table:** `inventory`  
**Package:** `com.vendo.inventory.entity`  
**Required Fields:**
- id, product_id, retailer_id, warehouse_id
- quantity, reserved_quantity
- available_quantity (computed: quantity - reserved_quantity)
- low_stock_threshold, reorder_point
- created_at, updated_at
- UNIQUE(product_id, retailer_id, warehouse_id)

**Relationships:**
- @ManyToOne to Product
- @ManyToOne to Retailer
- @ManyToOne to Warehouse
- @OneToMany to StockMovement

### 12. StockMovement Entity
**Table:** `stock_movements`  
**Package:** `com.vendo.inventory.entity`  
**Required Fields:**
- id, inventory_id
- movement_type (IN, OUT, ADJUSTMENT, RETURN)
- quantity, reference_type, reference_id
- notes, created_by, created_at

**Relationships:**
- @ManyToOne to Inventory
- @ManyToOne to User (created_by)

### 13. CartItem Entity
**Table:** `cart_items`  
**Package:** `com.vendo.cart.entity`  
**Required Fields:**
- id, cart_id, product_id, retailer_id
- quantity, unit_price
- created_at, updated_at
- UNIQUE(cart_id, product_id, retailer_id)

**Relationships:**
- @ManyToOne to Cart
- @ManyToOne to Product
- @ManyToOne to Retailer

## Next Steps

1. Create missing B2B entities (BusinessAccount, BusinessContact, B2BPricingTier, PurchaseOrder)
2. Create missing Retailer entities (Retailer, RetailerBankAccount, ProductRetailer, RetailerReview)
3. Create missing Inventory entities (Warehouse, Inventory, StockMovement)
4. Create CartItem entity
5. Update existing entities to match schema exactly
6. Add missing fields and relationships
7. Update table names to match schema (remove `ec_` prefix where applicable)
