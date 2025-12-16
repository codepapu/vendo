-- Create order return items table
CREATE TABLE IF NOT EXISTS ec_order_return_items (
    id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,
    order_product_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(191) NOT NULL,
    product_image VARCHAR(191),
    qty INT NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    refund_amount DECIMAL(12,2) DEFAULT 0.00,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (return_id) REFERENCES ec_order_returns(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_order_return_items_return_id ON ec_order_return_items(return_id);
CREATE INDEX IF NOT EXISTS idx_order_return_items_product_id ON ec_order_return_items(product_id);
