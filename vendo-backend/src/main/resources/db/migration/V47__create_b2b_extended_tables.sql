-- ============================================
-- B2B Extended Tables Migration
-- Creates tables for Quotes, Recurring Orders, Order Templates, Price Lists, and Bulk Orders
-- ============================================

-- ============================================
-- 1. Quotes Table (B2B Quote Generation)
-- ============================================
CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,
    quote_number VARCHAR(50) UNIQUE NOT NULL,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED, CONVERTED
    subtotal DECIMAL(15,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    expiration_date DATE,
    terms TEXT,
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    converted_to_order_id BIGINT REFERENCES orders(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quote Line Items
CREATE TABLE quote_items (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT REFERENCES quotes(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    total_price DECIMAL(15,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quote Revisions (for tracking quote history)
CREATE TABLE quote_revisions (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT REFERENCES quotes(id) ON DELETE CASCADE,
    revision_number INTEGER NOT NULL,
    status VARCHAR(50),
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(quote_id, revision_number)
);

-- ============================================
-- 2. Recurring Orders Table
-- ============================================
CREATE TABLE recurring_orders (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    recurrence_pattern VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, QUARTERLY
    recurrence_value INTEGER DEFAULT 1, -- e.g., every 2 weeks, every 3 months
    start_date DATE NOT NULL,
    end_date DATE,
    next_order_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, CANCELLED, COMPLETED
    notification_days_before INTEGER DEFAULT 3, -- notify X days before next order
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recurring Order Items
CREATE TABLE recurring_order_items (
    id BIGSERIAL PRIMARY KEY,
    recurring_order_id BIGINT REFERENCES recurring_orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Recurring Order History (tracks generated orders)
CREATE TABLE recurring_order_history (
    id BIGSERIAL PRIMARY KEY,
    recurring_order_id BIGINT REFERENCES recurring_orders(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id),
    scheduled_date DATE NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'GENERATED' -- GENERATED, FAILED, SKIPPED
);

-- ============================================
-- 3. Order Templates Table
-- ============================================
CREATE TABLE order_templates (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_shared BOOLEAN DEFAULT false, -- shared within business account
    created_by BIGINT REFERENCES users(id),
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order Template Items
CREATE TABLE order_template_items (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT REFERENCES order_templates(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2),
    notes TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. Price Lists Table (Custom pricing per business account)
-- ============================================
CREATE TABLE price_lists (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INTEGER DEFAULT 1,
    effective_from DATE NOT NULL,
    effective_until DATE,
    is_active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Price List Items (products with custom prices)
CREATE TABLE price_list_items (
    id BIGSERIAL PRIMARY KEY,
    price_list_id BIGINT REFERENCES price_lists(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    price DECIMAL(10,2) NOT NULL,
    min_quantity INTEGER DEFAULT 1,
    max_quantity INTEGER,
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(price_list_id, product_id, min_quantity)
);

-- Price List Assignments (assign price lists to business accounts)
CREATE TABLE price_list_assignments (
    id BIGSERIAL PRIMARY KEY,
    price_list_id BIGINT REFERENCES price_lists(id) ON DELETE CASCADE,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT true,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id),
    UNIQUE(price_list_id, business_account_id)
);

-- ============================================
-- 5. Bulk Orders Table
-- ============================================
CREATE TABLE bulk_orders (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_file_name VARCHAR(255), -- original CSV/Excel filename
    source_file_type VARCHAR(50), -- CSV, XLSX, XLS
    total_items INTEGER DEFAULT 0,
    processed_items INTEGER DEFAULT 0,
    failed_items INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, VALIDATING, PROCESSING, COMPLETED, FAILED, PARTIAL
    validation_errors JSONB, -- store validation errors
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bulk Order Items (individual items from bulk import)
CREATE TABLE bulk_order_items (
    id BIGSERIAL PRIMARY KEY,
    bulk_order_id BIGINT REFERENCES bulk_orders(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL, -- row number in original file
    product_id BIGINT REFERENCES products(id),
    product_sku VARCHAR(100),
    product_name VARCHAR(255),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(15,2),
    order_id BIGINT REFERENCES orders(id), -- created order if successful
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, VALIDATED, PROCESSED, FAILED
    validation_errors TEXT, -- validation errors for this item
    processing_errors TEXT, -- processing errors for this item
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- Quotes Indexes
CREATE INDEX idx_quotes_business_account_id ON quotes(business_account_id);
CREATE INDEX idx_quotes_status ON quotes(status);
CREATE INDEX idx_quotes_quote_number ON quotes(quote_number);
CREATE INDEX idx_quotes_expiration_date ON quotes(expiration_date);
CREATE INDEX idx_quotes_created_at ON quotes(created_at);
CREATE INDEX idx_quote_items_quote_id ON quote_items(quote_id);
CREATE INDEX idx_quote_items_product_id ON quote_items(product_id);
CREATE INDEX idx_quote_revisions_quote_id ON quote_revisions(quote_id);

-- Recurring Orders Indexes
CREATE INDEX idx_recurring_orders_business_account_id ON recurring_orders(business_account_id);
CREATE INDEX idx_recurring_orders_status ON recurring_orders(status);
CREATE INDEX idx_recurring_orders_next_order_date ON recurring_orders(next_order_date);
CREATE INDEX idx_recurring_orders_created_by ON recurring_orders(created_by);
CREATE INDEX idx_recurring_order_items_recurring_order_id ON recurring_order_items(recurring_order_id);
CREATE INDEX idx_recurring_order_items_product_id ON recurring_order_items(product_id);
CREATE INDEX idx_recurring_order_history_recurring_order_id ON recurring_order_history(recurring_order_id);
CREATE INDEX idx_recurring_order_history_order_id ON recurring_order_history(order_id);
CREATE INDEX idx_recurring_order_history_scheduled_date ON recurring_order_history(scheduled_date);

-- Order Templates Indexes
CREATE INDEX idx_order_templates_business_account_id ON order_templates(business_account_id);
CREATE INDEX idx_order_templates_created_by ON order_templates(created_by);
CREATE INDEX idx_order_templates_is_shared ON order_templates(is_shared);
CREATE INDEX idx_order_template_items_template_id ON order_template_items(template_id);
CREATE INDEX idx_order_template_items_product_id ON order_template_items(product_id);

-- Price Lists Indexes
CREATE INDEX idx_price_lists_business_account_id ON price_lists(business_account_id);
CREATE INDEX idx_price_lists_is_active ON price_lists(is_active);
CREATE INDEX idx_price_lists_effective_from ON price_lists(effective_from);
CREATE INDEX idx_price_lists_effective_until ON price_lists(effective_until);
CREATE INDEX idx_price_list_items_price_list_id ON price_list_items(price_list_id);
CREATE INDEX idx_price_list_items_product_id ON price_list_items(product_id);
CREATE INDEX idx_price_list_assignments_price_list_id ON price_list_assignments(price_list_id);
CREATE INDEX idx_price_list_assignments_business_account_id ON price_list_assignments(business_account_id);

-- Bulk Orders Indexes
CREATE INDEX idx_bulk_orders_business_account_id ON bulk_orders(business_account_id);
CREATE INDEX idx_bulk_orders_status ON bulk_orders(status);
CREATE INDEX idx_bulk_orders_created_by ON bulk_orders(created_by);
CREATE INDEX idx_bulk_order_items_bulk_order_id ON bulk_order_items(bulk_order_id);
CREATE INDEX idx_bulk_order_items_product_id ON bulk_order_items(product_id);
CREATE INDEX idx_bulk_order_items_status ON bulk_order_items(status);
CREATE INDEX idx_bulk_order_items_order_id ON bulk_order_items(order_id);

