-- Create product attributes table
CREATE TABLE IF NOT EXISTS ec_product_attributes (
    id BIGSERIAL PRIMARY KEY,
    attribute_set_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    slug VARCHAR(120),
    color VARCHAR(120),
    image VARCHAR(191),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    "order" INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attribute_set_id) REFERENCES ec_product_attribute_sets(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_attributes_attribute_set_id ON ec_product_attributes(attribute_set_id);
CREATE INDEX IF NOT EXISTS idx_product_attributes_set_order_id ON ec_product_attributes(attribute_set_id, "order", id);
