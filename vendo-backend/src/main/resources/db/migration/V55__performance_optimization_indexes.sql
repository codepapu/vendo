-- Performance Optimization: Additional Database Indexes
-- This migration adds indexes on frequently queried columns to improve query performance

-- Product indexes for filtering and sorting
CREATE INDEX IF NOT EXISTS idx_products_status_variation ON products(status, is_variation) WHERE is_variation = false;
CREATE INDEX IF NOT EXISTS idx_products_status_featured ON products(status, is_featured) WHERE is_featured = true AND is_variation = false;
CREATE INDEX IF NOT EXISTS idx_products_price_range ON products(status, COALESCE(sale_price, price)) WHERE is_variation = false;
CREATE INDEX IF NOT EXISTS idx_products_name_search ON products USING gin(to_tsvector('english', name)) WHERE is_variation = false;
CREATE INDEX IF NOT EXISTS idx_products_description_search ON products USING gin(to_tsvector('english', description)) WHERE is_variation = false;
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC) WHERE is_variation = false;

-- Product category relationship indexes
CREATE INDEX IF NOT EXISTS idx_product_category_product_status ON product_category_product(product_id, category_id);
CREATE INDEX IF NOT EXISTS idx_product_category_category_status ON product_category_product(category_id, product_id);

-- Product review indexes for rating calculations
CREATE INDEX IF NOT EXISTS idx_product_reviews_product_approved ON product_reviews(product_id, is_approved) WHERE is_approved = true;
CREATE INDEX IF NOT EXISTS idx_product_reviews_rating ON product_reviews(product_id, rating) WHERE is_approved = true;

-- Order item indexes for retailer dashboard queries
CREATE INDEX IF NOT EXISTS idx_order_items_retailer_created ON order_items(retailer_id, created_at DESC) WHERE retailer_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_order_items_retailer_status ON order_items(retailer_id, order_id) WHERE retailer_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_order_items_order_retailer ON order_items(order_id, retailer_id) WHERE retailer_id IS NOT NULL;

-- Order indexes for status and date filtering
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_orders_business_account_status ON orders(business_account_id, status) WHERE business_account_id IS NOT NULL;

-- Retailer dashboard optimization indexes
CREATE INDEX IF NOT EXISTS idx_product_retailers_retailer_active ON product_retailers(retailer_id, is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_product_retailers_retailer_stock ON product_retailers(retailer_id, stock_quantity) WHERE stock_quantity <= low_stock_threshold;

-- Commission tracking indexes
CREATE INDEX IF NOT EXISTS idx_commission_tracking_retailer_status_date ON commission_tracking(retailer_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_commission_tracking_settlement ON commission_tracking(retailer_id, settlement_date) WHERE settlement_date IS NOT NULL;

-- B2B pricing tier indexes
CREATE INDEX IF NOT EXISTS idx_b2b_pricing_tiers_product_active ON b2b_pricing_tiers(product_id, is_active, min_quantity) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_b2b_pricing_tiers_product_business ON b2b_pricing_tiers(product_id, business_type, min_quantity) WHERE is_active = true;

-- Business account indexes
CREATE INDEX IF NOT EXISTS idx_business_accounts_user_status ON business_accounts(user_id, status) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_business_accounts_type_status ON business_accounts(business_type, status);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_products_brand_status ON products(brand_id, status, is_variation) WHERE brand_id IS NOT NULL AND is_variation = false;
CREATE INDEX IF NOT EXISTS idx_orders_type_status_created ON orders(order_type, status, created_at DESC);

-- Full-text search indexes (if PostgreSQL version supports)
-- These improve search performance for product names and descriptions
-- Note: Requires pg_trgm extension for trigram matching
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pg_trgm') THEN
        CREATE EXTENSION IF NOT EXISTS pg_trgm;
        CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING gin(name gin_trgm_ops) WHERE is_variation = false;
        CREATE INDEX IF NOT EXISTS idx_products_description_trgm ON products USING gin(description gin_trgm_ops) WHERE is_variation = false;
    END IF;
END $$;

-- Analyze tables to update statistics
ANALYZE products;
ANALYZE order_items;
ANALYZE orders;
ANALYZE product_retailers;
ANALYZE commission_tracking;
ANALYZE b2b_pricing_tiers;
ANALYZE business_accounts;

