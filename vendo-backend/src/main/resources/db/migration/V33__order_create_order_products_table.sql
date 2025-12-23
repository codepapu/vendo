-- Create order_products table
CREATE TABLE IF NOT EXISTS ec_order_products (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(191) NOT NULL,
    product_image VARCHAR(191),
    qty INT NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0.00,
    options TEXT,
    product_options TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES ec_orders(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_order_products_order_id ON ec_order_products(order_id);
CREATE INDEX IF NOT EXISTS idx_order_products_product_id ON ec_order_products(product_id);
