-- Create product variation items table (links variations to attributes)
CREATE TABLE IF NOT EXISTS ec_product_variation_items (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL,
    variation_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attribute_id) REFERENCES ec_product_attributes(id) ON DELETE CASCADE,
    FOREIGN KEY (variation_id) REFERENCES ec_product_variations(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_variation_items_attribute_id ON ec_product_variation_items(attribute_id);
CREATE INDEX IF NOT EXISTS idx_variation_items_variation_id ON ec_product_variation_items(variation_id);
