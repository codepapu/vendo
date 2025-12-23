-- Create wishlists table
CREATE TABLE IF NOT EXISTS ec_wish_lists (
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id, product_id),
    -- Note: customer_id references ec_customers but table is in user-service
    -- Foreign key constraint can be added when both services share same database
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_wishlists_product_customer ON ec_wish_lists(product_id, customer_id);
CREATE INDEX IF NOT EXISTS idx_wishlists_customer_id ON ec_wish_lists(customer_id);
