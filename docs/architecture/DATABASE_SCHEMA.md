# Database Schema Design

## Enhanced Schema for B2B, B2C, and Retailer Models

## Core Tables

### 1. Users & Authentication

```sql
-- Enhanced User table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    user_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- CUSTOMER, BUSINESS, RETAILER, ADMIN
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Business Accounts (B2B)
CREATE TABLE business_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    business_type VARCHAR(50), -- CORPORATE, SME, ENTERPRISE
    credit_limit DECIMAL(15,2) DEFAULT 0,
    credit_terms_days INTEGER DEFAULT 30,
    payment_terms TEXT,
    billing_address_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, SUSPENDED
    approved_at TIMESTAMP,
    approved_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Business Contacts
CREATE TABLE business_contacts (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id),
    contact_type VARCHAR(50), -- PRIMARY, BILLING, SHIPPING, PURCHASER
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Retailer/Vendor Accounts
CREATE TABLE retailers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    store_name VARCHAR(255) NOT NULL,
    store_slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    banner_url VARCHAR(500),
    commission_rate DECIMAL(5,2) DEFAULT 0, -- Platform commission percentage
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ACTIVE, SUSPENDED, REJECTED
    rating DECIMAL(3,2) DEFAULT 0,
    total_sales DECIMAL(15,2) DEFAULT 0,
    total_orders INTEGER DEFAULT 0,
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Retailer Bank Accounts
CREATE TABLE retailer_bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE CASCADE,
    account_holder_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    ifsc_code VARCHAR(50),
    swift_code VARCHAR(50),
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Products & Catalog

```sql
-- Enhanced Product table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    sku VARCHAR(100) UNIQUE NOT NULL,
    barcode VARCHAR(100),
    product_type VARCHAR(50) DEFAULT 'SIMPLE', -- SIMPLE, VARIABLE, BUNDLE, DIGITAL
    status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, ACTIVE, INACTIVE, ARCHIVED
    is_featured BOOLEAN DEFAULT false,
    weight DECIMAL(10,2),
    dimensions JSONB, -- {length, width, height, unit}
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product Ownership (for retailers)
CREATE TABLE product_retailers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE CASCADE,
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2),
    cost_price DECIMAL(10,2), -- Retailer's cost
    stock_quantity INTEGER DEFAULT 0,
    low_stock_threshold INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, retailer_id)
);

-- B2B Pricing Tiers
CREATE TABLE b2b_pricing_tiers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    business_type VARCHAR(50), -- CORPORATE, SME, ENTERPRISE
    min_quantity INTEGER NOT NULL,
    max_quantity INTEGER,
    price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product Category Relationship (Many-to-Many)
CREATE TABLE product_category_product (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);

-- Product Attributes
CREATE TABLE product_attributes (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product Variations (for configurable products)
CREATE TABLE product_variations (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id), -- The variation product
    configurable_product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE, -- The parent configurable product
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, configurable_product_id)
);

-- Product Variation Items (links variations to attributes)
CREATE TABLE product_variation_items (
    id BIGSERIAL PRIMARY KEY,
    variation_id BIGINT NOT NULL REFERENCES product_variations(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES product_attributes(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wishlists
CREATE TABLE wishlists (
    customer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id, product_id)
);
```

### 3. Orders & Transactions

```sql
-- Enhanced Order table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id),
    business_account_id BIGINT REFERENCES business_accounts(id),
    order_type VARCHAR(20) NOT NULL, -- B2C, B2B, RETAILER
    status VARCHAR(50) NOT NULL, -- PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    payment_status VARCHAR(50), -- PENDING, PAID, PARTIAL, REFUNDED, FAILED
    payment_method VARCHAR(50),
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    shipping_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order Items
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    retailer_id BIGINT REFERENCES retailers(id), -- For marketplace orders
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    commission_amount DECIMAL(15,2) DEFAULT 0, -- Platform commission
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- B2B Purchase Orders
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    business_account_id BIGINT REFERENCES business_accounts(id),
    order_id BIGINT REFERENCES orders(id),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, COMPLETED
    requested_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approval_notes TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Invoices (B2B)
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT REFERENCES orders(id),
    business_account_id BIGINT REFERENCES business_accounts(id),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PAID, OVERDUE, CANCELLED
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payments
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    invoice_id BIGINT REFERENCES invoices(id),
    payment_method VARCHAR(50) NOT NULL,
    payment_gateway VARCHAR(50),
    transaction_id VARCHAR(255),
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) NOT NULL, -- PENDING, COMPLETED, FAILED, REFUNDED
    gateway_response JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. Cart & Checkout

```sql
-- Shopping Carts
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    business_account_id BIGINT REFERENCES business_accounts(id),
    session_id VARCHAR(255), -- For guest carts
    cart_type VARCHAR(20) DEFAULT 'B2C', -- B2C, B2B
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart Items
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    retailer_id BIGINT REFERENCES retailers(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id, retailer_id)
);
```

### 5. Addresses & Shipping

```sql
-- Addresses
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    business_account_id BIGINT REFERENCES business_accounts(id),
    address_type VARCHAR(50), -- SHIPPING, BILLING, BOTH
    contact_name VARCHAR(255),
    phone VARCHAR(20),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shipping Methods
CREATE TABLE shipping_methods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    carrier VARCHAR(100),
    base_cost DECIMAL(10,2) NOT NULL,
    cost_per_kg DECIMAL(10,2),
    estimated_days INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shipments
CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    shipping_method_id BIGINT REFERENCES shipping_methods(id),
    tracking_number VARCHAR(255),
    carrier VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, IN_TRANSIT, DELIVERED, FAILED
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6. Inventory Management

```sql
-- Warehouses
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    address_id BIGINT REFERENCES addresses(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    retailer_id BIGINT REFERENCES retailers(id),
    warehouse_id BIGINT REFERENCES warehouses(id),
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    available_quantity INTEGER GENERATED ALWAYS AS (quantity - reserved_quantity) STORED,
    low_stock_threshold INTEGER DEFAULT 10,
    reorder_point INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, retailer_id, warehouse_id)
);

-- Stock Movements
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT REFERENCES inventory(id),
    movement_type VARCHAR(50) NOT NULL, -- IN, OUT, ADJUSTMENT, RETURN
    quantity INTEGER NOT NULL,
    reference_type VARCHAR(50), -- ORDER, RETURN, ADJUSTMENT
    reference_id BIGINT,
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 7. Reviews & Ratings

```sql
-- Product Reviews
CREATE TABLE product_reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    retailer_id BIGINT REFERENCES retailers(id),
    user_id BIGINT REFERENCES users(id),
    order_id BIGINT REFERENCES orders(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    review_text TEXT,
    is_verified_purchase BOOLEAN DEFAULT false,
    is_approved BOOLEAN DEFAULT false,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Retailer Reviews
CREATE TABLE retailer_reviews (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id),
    order_id BIGINT REFERENCES orders(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    is_approved BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 8. Promotions & Discounts

```sql
-- Coupons
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(50) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    discount_value DECIMAL(10,2) NOT NULL,
    minimum_purchase DECIMAL(10,2),
    maximum_discount DECIMAL(10,2),
    usage_limit INTEGER,
    used_count INTEGER DEFAULT 0,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    applicable_to VARCHAR(50), -- ALL, B2C, B2B, SPECIFIC_PRODUCTS, SPECIFIC_CATEGORIES
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- B2B Volume Discounts
CREATE TABLE volume_discounts (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    min_quantity INTEGER NOT NULL,
    discount_percentage DECIMAL(5,2) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Indexes for Performance

```sql
-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Product indexes
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_retailer_id ON product_retailers(retailer_id);
CREATE INDEX idx_products_category ON products(id);
CREATE INDEX idx_product_category_product_product_id ON product_category_product(product_id);
CREATE INDEX idx_product_category_product_category_id ON product_category_product(category_id);

-- Product Variation indexes
CREATE INDEX idx_product_variations_product_configurable ON product_variations(product_id, configurable_product_id);
CREATE INDEX idx_product_variations_configurable_product_id ON product_variations(configurable_product_id);
CREATE INDEX idx_product_variation_items_variation_id ON product_variation_items(variation_id);
CREATE INDEX idx_product_variation_items_attribute_id ON product_variation_items(attribute_id);

-- Wishlist indexes
CREATE INDEX idx_wishlists_product_customer ON wishlists(product_id, customer_id);
CREATE INDEX idx_wishlists_customer_id ON wishlists(customer_id);

-- Order indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_business_account_id ON orders(business_account_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);

-- Inventory indexes
CREATE INDEX idx_inventory_product_retailer ON inventory(product_id, retailer_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);
```

## Database Migrations

All schema changes should be managed through Flyway migrations in:
`vendo-backend/src/main/resources/db/migration/`
