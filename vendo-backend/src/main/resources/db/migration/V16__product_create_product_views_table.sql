-- Create product views table (for analytics)
CREATE TABLE IF NOT EXISTS ec_product_views (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    views INT NOT NULL DEFAULT 1,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE,
    UNIQUE (product_id, date)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_views_product_id ON ec_product_views(product_id);
CREATE INDEX IF NOT EXISTS idx_product_views_date ON ec_product_views(date);
