-- Create customer used coupons table
CREATE TABLE IF NOT EXISTS ec_customer_used_coupons (
    discount_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (discount_id, customer_id)
    -- Note: Foreign keys can be added when services share same database
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_customer_used_coupons_discount_id ON ec_customer_used_coupons(discount_id);
CREATE INDEX IF NOT EXISTS idx_customer_used_coupons_customer_id ON ec_customer_used_coupons(customer_id);
