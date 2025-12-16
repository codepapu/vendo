-- Create customer recently viewed products table
CREATE TABLE IF NOT EXISTS ec_customer_recently_viewed_products (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- Note: customer_id references ec_customers but table is in user-service
    -- Note: product_id references ec_products but table is in product-service
    -- Foreign key constraints can be added when services share same database
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_recently_viewed_customer_id ON ec_customer_recently_viewed_products(customer_id);
CREATE INDEX IF NOT EXISTS idx_recently_viewed_product_id ON ec_customer_recently_viewed_products(product_id);
CREATE INDEX IF NOT EXISTS idx_recently_viewed_customer_created ON ec_customer_recently_viewed_products(customer_id, created_at);
