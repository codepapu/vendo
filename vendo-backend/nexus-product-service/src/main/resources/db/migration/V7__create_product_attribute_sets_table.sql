-- Create product attribute sets table
CREATE TABLE IF NOT EXISTS ec_product_attribute_sets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(191) NOT NULL,
    slug VARCHAR(191) UNIQUE,
    display_layout VARCHAR(60) NOT NULL DEFAULT 'dropdown',
    is_searchable BOOLEAN NOT NULL DEFAULT FALSE,
    is_comparable BOOLEAN NOT NULL DEFAULT FALSE,
    is_use_in_product_listing BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_attribute_sets_status ON ec_product_attribute_sets(status);
CREATE INDEX IF NOT EXISTS idx_attribute_sets_slug ON ec_product_attribute_sets(slug);
