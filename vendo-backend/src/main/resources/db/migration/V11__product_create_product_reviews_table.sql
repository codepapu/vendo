-- Create product reviews table
CREATE TABLE IF NOT EXISTS ec_reviews (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    customer_name VARCHAR(191),
    customer_email VARCHAR(191),
    product_id BIGINT NOT NULL,
    star DECIMAL(3,2) NOT NULL,
    comment TEXT NOT NULL,
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    images TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Note: customer_id references ec_customers but table is in user-service
    -- Foreign key constraint can be added when both services share same database
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE,
    UNIQUE (product_id, customer_id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_reviews_product_customer_status_created ON ec_reviews(product_id, customer_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON ec_reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON ec_reviews(status);
