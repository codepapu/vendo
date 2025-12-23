-- Create taxes table
CREATE TABLE IF NOT EXISTS ec_taxes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(191) NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create tax_products relationship table
CREATE TABLE IF NOT EXISTS ec_tax_products (
    tax_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (tax_id, product_id),
    FOREIGN KEY (tax_id) REFERENCES ec_taxes(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_taxes_status ON ec_taxes(status);
CREATE INDEX IF NOT EXISTS idx_tax_products_tax_id ON ec_tax_products(tax_id);
CREATE INDEX IF NOT EXISTS idx_tax_products_product_id ON ec_tax_products(product_id);
