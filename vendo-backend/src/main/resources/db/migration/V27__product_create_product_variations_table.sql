-- Create product variations table
CREATE TABLE IF NOT EXISTS ec_product_variations (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    configurable_product_id BIGINT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE,
    FOREIGN KEY (configurable_product_id) REFERENCES ec_products(id) ON DELETE CASCADE,
    UNIQUE (product_id, configurable_product_id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_variations_product_configurable ON ec_product_variations(product_id, configurable_product_id);
CREATE INDEX IF NOT EXISTS idx_product_variations_configurable_product_id ON ec_product_variations(configurable_product_id);
