-- Create product files table
CREATE TABLE IF NOT EXISTS ec_product_files (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    url VARCHAR(400),
    extras TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_files_product_id ON ec_product_files(product_id);
