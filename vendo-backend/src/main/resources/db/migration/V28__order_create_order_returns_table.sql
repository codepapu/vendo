-- Create order returns table
CREATE TABLE IF NOT EXISTS ec_order_returns (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(191) UNIQUE,
    order_id BIGINT NOT NULL,
    store_id BIGINT,
    user_id BIGINT NOT NULL,
    reason TEXT,
    order_status VARCHAR(191),
    return_status VARCHAR(191) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES ec_orders(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_order_returns_code ON ec_order_returns(code);
CREATE INDEX IF NOT EXISTS idx_order_returns_order_id ON ec_order_returns(order_id);
CREATE INDEX IF NOT EXISTS idx_order_returns_user_id ON ec_order_returns(user_id);
CREATE INDEX IF NOT EXISTS idx_order_returns_return_status ON ec_order_returns(return_status);
