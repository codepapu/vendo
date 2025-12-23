-- Create product tags table
CREATE TABLE IF NOT EXISTS ec_product_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    description TEXT,
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create product_tag_product pivot table
CREATE TABLE IF NOT EXISTS ec_product_tag_product (
    product_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES ec_product_tags(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_tags_status ON ec_product_tags(status);
CREATE INDEX IF NOT EXISTS idx_product_tag_product_product_id ON ec_product_tag_product(product_id);
CREATE INDEX IF NOT EXISTS idx_product_tag_product_tag_id ON ec_product_tag_product(tag_id);
