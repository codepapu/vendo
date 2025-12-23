-- Create order histories table
CREATE TABLE IF NOT EXISTS ec_order_histories (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(120) NOT NULL,
    description VARCHAR(400),
    user_id BIGINT,
    order_id BIGINT NOT NULL,
    extras TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES ec_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_order_histories_order_id ON ec_order_histories(order_id);
CREATE INDEX IF NOT EXISTS idx_order_histories_user_id ON ec_order_histories(user_id);
CREATE INDEX IF NOT EXISTS idx_order_histories_created_at ON ec_order_histories(order_id, created_at);
