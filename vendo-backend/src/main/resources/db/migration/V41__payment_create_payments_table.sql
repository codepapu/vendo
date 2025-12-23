-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    currency VARCHAR(120),
    user_id BIGINT NOT NULL DEFAULT 0,
    charge_id VARCHAR(191),
    payment_channel VARCHAR(60),
    description VARCHAR(400),
    amount DECIMAL(15,2) NOT NULL,
    payment_fee DECIMAL(15,2) DEFAULT 0.00,
    order_id BIGINT,
    status VARCHAR(60) DEFAULT 'pending',
    payment_type VARCHAR(191) DEFAULT 'confirm',
    customer_id BIGINT,
    refunded_amount DECIMAL(15,2),
    refund_note VARCHAR(191),
    customer_type VARCHAR(191),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES ec_orders(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_charge_id ON payments(charge_id);
