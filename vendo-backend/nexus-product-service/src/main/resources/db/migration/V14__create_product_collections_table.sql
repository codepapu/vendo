-- Create product collections table
CREATE TABLE IF NOT EXISTS ec_product_collections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    slug VARCHAR(191) NOT NULL UNIQUE,
    description VARCHAR(400),
    image VARCHAR(191),
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create product_collection_products pivot table
CREATE TABLE IF NOT EXISTS ec_product_collection_products (
    product_collection_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, product_collection_id),
    FOREIGN KEY (product_collection_id) REFERENCES ec_product_collections(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_product_collections_slug ON ec_product_collections(slug);
CREATE INDEX IF NOT EXISTS idx_product_collections_status ON ec_product_collections(status);
CREATE INDEX IF NOT EXISTS idx_product_collection_products_collection_id ON ec_product_collection_products(product_collection_id);
CREATE INDEX IF NOT EXISTS idx_product_collection_products_product_id ON ec_product_collection_products(product_id);
