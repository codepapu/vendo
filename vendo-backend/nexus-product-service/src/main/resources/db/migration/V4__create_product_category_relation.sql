-- Create product_category pivot table
CREATE TABLE IF NOT EXISTS ec_product_category_product (
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_category_product_id ON ec_product_category_product(product_id);
CREATE INDEX IF NOT EXISTS idx_product_category_category_id ON ec_product_category_product(category_id);
