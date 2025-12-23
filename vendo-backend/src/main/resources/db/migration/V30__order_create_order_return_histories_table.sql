-- Create order return histories table
CREATE TABLE IF NOT EXISTS ec_order_return_histories (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,
    action VARCHAR(191) NOT NULL,
    description VARCHAR(400),
    reason VARCHAR(191),
    user_id BIGINT,
    extras TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (return_id) REFERENCES ec_order_returns(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_order_return_histories_return_id ON ec_order_return_histories(return_id);
CREATE INDEX IF NOT EXISTS idx_order_return_histories_user_id ON ec_order_return_histories(user_id);
