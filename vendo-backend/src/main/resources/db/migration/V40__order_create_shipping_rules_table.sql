-- Create shipping rules table
CREATE TABLE IF NOT EXISTS ec_shipping_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    shipping_id BIGINT NOT NULL,
    type VARCHAR(60) NOT NULL DEFAULT 'base_on_price',
    from_value DECIMAL(15,2) DEFAULT 0.00,
    to_value DECIMAL(15,2),
    price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shipping_id) REFERENCES ec_shipping(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_shipping_rules_shipping_id ON ec_shipping_rules(shipping_id);
CREATE INDEX IF NOT EXISTS idx_shipping_rules_type ON ec_shipping_rules(type);
