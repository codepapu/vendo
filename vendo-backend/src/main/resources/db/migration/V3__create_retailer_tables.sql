-- Migration: V3__create_retailer_tables.sql
-- Description: Create Retailer/Marketplace functionality tables including retailers, 
--              retailer_bank_accounts, product_retailers, retailer_reviews, and commission_tracking
-- Date: Created for Retailer/Marketplace feature implementation

-- ============================================
-- 1. Retailers Table
-- ============================================
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

-- ============================================
-- 2. Retailer Bank Accounts Table
-- ============================================
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

-- ============================================
-- 3. Product Retailers Table (Product Ownership)
-- ============================================
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

-- ============================================
-- 4. Retailer Reviews Table
-- ============================================
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

-- ============================================
-- 5. Commission Tracking Table
-- ============================================
CREATE TABLE commission_tracking (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id),
    order_item_id BIGINT REFERENCES order_items(id),
    commission_amount DECIMAL(15,2) NOT NULL,
    commission_rate DECIMAL(5,2), -- Commission rate at time of transaction
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PAID, SETTLED, CANCELLED
    settlement_date DATE,
    payment_reference VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- Retailers Indexes
CREATE INDEX idx_retailers_user_id ON retailers(user_id);
CREATE INDEX idx_retailers_store_slug ON retailers(store_slug);
CREATE INDEX idx_retailers_status ON retailers(status);
CREATE INDEX idx_retailers_verified ON retailers(verified);
CREATE INDEX idx_retailers_created_at ON retailers(created_at);

-- Retailer Bank Accounts Indexes
CREATE INDEX idx_retailer_bank_accounts_retailer_id ON retailer_bank_accounts(retailer_id);
CREATE INDEX idx_retailer_bank_accounts_is_primary ON retailer_bank_accounts(is_primary);

-- Product Retailers Indexes
CREATE INDEX idx_product_retailers_product_id ON product_retailers(product_id);
CREATE INDEX idx_product_retailers_retailer_id ON product_retailers(retailer_id);
CREATE INDEX idx_product_retailers_status ON product_retailers(is_active);
CREATE INDEX idx_product_retailers_product_retailer ON product_retailers(product_id, retailer_id);

-- Retailer Reviews Indexes
CREATE INDEX idx_retailer_reviews_retailer_id ON retailer_reviews(retailer_id);
CREATE INDEX idx_retailer_reviews_user_id ON retailer_reviews(user_id);
CREATE INDEX idx_retailer_reviews_order_id ON retailer_reviews(order_id);
CREATE INDEX idx_retailer_reviews_rating ON retailer_reviews(rating);
CREATE INDEX idx_retailer_reviews_is_approved ON retailer_reviews(is_approved);
CREATE INDEX idx_retailer_reviews_created_at ON retailer_reviews(created_at);

-- Commission Tracking Indexes
CREATE INDEX idx_commission_tracking_retailer_id ON commission_tracking(retailer_id);
CREATE INDEX idx_commission_tracking_order_id ON commission_tracking(order_id);
CREATE INDEX idx_commission_tracking_order_item_id ON commission_tracking(order_item_id);
CREATE INDEX idx_commission_tracking_status ON commission_tracking(status);
CREATE INDEX idx_commission_tracking_settlement_date ON commission_tracking(settlement_date);
CREATE INDEX idx_commission_tracking_created_at ON commission_tracking(created_at);

-- ============================================
-- Additional Constraints
-- ============================================

-- Ensure commission_rate is between 0 and 100 for retailers
ALTER TABLE retailers ADD CONSTRAINT chk_retailers_commission_rate 
    CHECK (commission_rate >= 0 AND commission_rate <= 100);

-- Ensure rating is between 0 and 5 for retailers
ALTER TABLE retailers ADD CONSTRAINT chk_retailers_rating 
    CHECK (rating >= 0 AND rating <= 5);

-- Ensure total_sales is non-negative for retailers
ALTER TABLE retailers ADD CONSTRAINT chk_retailers_total_sales 
    CHECK (total_sales >= 0);

-- Ensure total_orders is non-negative for retailers
ALTER TABLE retailers ADD CONSTRAINT chk_retailers_total_orders 
    CHECK (total_orders >= 0);

-- Ensure price is positive for product_retailers
ALTER TABLE product_retailers ADD CONSTRAINT chk_product_retailers_price 
    CHECK (price > 0);

-- Ensure compare_at_price is positive if set for product_retailers
ALTER TABLE product_retailers ADD CONSTRAINT chk_product_retailers_compare_at_price 
    CHECK (compare_at_price IS NULL OR compare_at_price > 0);

-- Ensure cost_price is non-negative if set for product_retailers
ALTER TABLE product_retailers ADD CONSTRAINT chk_product_retailers_cost_price 
    CHECK (cost_price IS NULL OR cost_price >= 0);

-- Ensure stock_quantity is non-negative for product_retailers
ALTER TABLE product_retailers ADD CONSTRAINT chk_product_retailers_stock_quantity 
    CHECK (stock_quantity >= 0);

-- Ensure low_stock_threshold is non-negative for product_retailers
ALTER TABLE product_retailers ADD CONSTRAINT chk_product_retailers_low_stock_threshold 
    CHECK (low_stock_threshold >= 0);

-- Ensure commission_amount is non-negative for commission_tracking
ALTER TABLE commission_tracking ADD CONSTRAINT chk_commission_tracking_commission_amount 
    CHECK (commission_amount >= 0);

-- Ensure commission_rate is between 0 and 100 if set for commission_tracking
ALTER TABLE commission_tracking ADD CONSTRAINT chk_commission_tracking_commission_rate 
    CHECK (commission_rate IS NULL OR (commission_rate >= 0 AND commission_rate <= 100));

-- ============================================
-- Comments for Documentation
-- ============================================
COMMENT ON TABLE retailers IS 'Stores retailer/vendor account information for marketplace sellers';
COMMENT ON TABLE retailer_bank_accounts IS 'Stores bank account information for retailers to receive payments';
COMMENT ON TABLE product_retailers IS 'Links products to retailers with pricing and inventory information';
COMMENT ON TABLE retailer_reviews IS 'Stores customer reviews and ratings for retailers';
COMMENT ON TABLE commission_tracking IS 'Tracks platform commissions from retailer sales and settlements';

COMMENT ON COLUMN retailers.status IS 'Retailer status: PENDING, ACTIVE, SUSPENDED, or REJECTED';
COMMENT ON COLUMN retailers.commission_rate IS 'Platform commission percentage (0-100)';
COMMENT ON COLUMN product_retailers.is_active IS 'Whether this product listing is active for the retailer';
COMMENT ON COLUMN retailer_reviews.rating IS 'Rating value from 1 to 5';
COMMENT ON COLUMN commission_tracking.status IS 'Commission status: PENDING, PAID, SETTLED, or CANCELLED';
