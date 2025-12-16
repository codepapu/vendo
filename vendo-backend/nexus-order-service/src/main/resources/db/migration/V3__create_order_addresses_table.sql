-- Create order addresses table
CREATE TABLE IF NOT EXISTS ec_order_addresses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(191),
    country VARCHAR(120),
    state VARCHAR(120),
    city VARCHAR(120),
    address VARCHAR(191),
    zip_code VARCHAR(20),
    order_id BIGINT NOT NULL,
    type VARCHAR(60) NOT NULL DEFAULT 'shipping_address',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES ec_orders(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_order_addresses_order_id ON ec_order_addresses(order_id);
CREATE INDEX IF NOT EXISTS idx_order_addresses_type ON ec_order_addresses(order_id, type);
