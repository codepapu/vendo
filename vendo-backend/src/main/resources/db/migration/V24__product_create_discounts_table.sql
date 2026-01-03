-- Create discounts table
CREATE TABLE IF NOT EXISTS ec_discounts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120),
    code VARCHAR(20) UNIQUE,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    quantity INT,
    total_used INT NOT NULL DEFAULT 0,
    value DECIMAL(15,2),
    type VARCHAR(60) DEFAULT 'coupon',
    can_use_with_promotion BOOLEAN NOT NULL DEFAULT FALSE,
    can_use_with_flash_sale BOOLEAN NOT NULL DEFAULT FALSE,
    discount_on VARCHAR(20),
    product_quantity INT,
    type_option VARCHAR(100) NOT NULL DEFAULT 'amount',
    target VARCHAR(100) NOT NULL DEFAULT 'all-orders',
    min_order_price DECIMAL(15,2),
    apply_via_url BOOLEAN NOT NULL DEFAULT FALSE,
    display_at_checkout BOOLEAN NOT NULL DEFAULT FALSE,
    store_id BIGINT,
    description VARCHAR(400),
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_discounts_code ON ec_discounts(code);
CREATE INDEX IF NOT EXISTS idx_discounts_status ON ec_discounts(status);
CREATE INDEX IF NOT EXISTS idx_discounts_start_date ON ec_discounts(start_date);
CREATE INDEX IF NOT EXISTS idx_discounts_end_date ON ec_discounts(end_date);
