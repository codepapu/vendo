-- Create orders table
CREATE TABLE IF NOT EXISTS ec_orders (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(191) UNIQUE,
    user_id BIGINT,
    shipping_option VARCHAR(60),
    shipping_method VARCHAR(60) NOT NULL DEFAULT 'default',
    status VARCHAR(120) NOT NULL DEFAULT 'pending',
    amount DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0.00,
    shipping_amount DECIMAL(15,2),
    payment_fee DECIMAL(15,2) DEFAULT 0.00,
    description TEXT,
    coupon_code VARCHAR(120),
    discount_amount DECIMAL(15,2),
    sub_total DECIMAL(15,2) NOT NULL,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    discount_description VARCHAR(191),
    is_finished BOOLEAN DEFAULT FALSE,
    cancellation_reason VARCHAR(191),
    cancellation_reason_description VARCHAR(191),
    completed_at TIMESTAMP,
    token VARCHAR(120),
    payment_id BIGINT,
    store_id BIGINT,
    proof_file VARCHAR(191),
    private_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_orders_user_status_created ON ec_orders(user_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_code ON ec_orders(code);
CREATE INDEX IF NOT EXISTS idx_orders_status ON ec_orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_store_id ON ec_orders(store_id);
