-- Create products table
CREATE TABLE IF NOT EXISTS ec_products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    description TEXT,
    content TEXT,
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    images TEXT,
    video_media TEXT,
    sku VARCHAR(191) UNIQUE,
    "order" INT NOT NULL DEFAULT 0,
    quantity INT,
    allow_checkout_when_out_of_stock BOOLEAN NOT NULL DEFAULT FALSE,
    with_storehouse_management BOOLEAN NOT NULL DEFAULT FALSE,
    stock_status VARCHAR(191) DEFAULT 'in_stock',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    brand_id BIGINT,
    is_variation BOOLEAN NOT NULL DEFAULT FALSE,
    sale_type INT NOT NULL DEFAULT 0,
    price DECIMAL(15,2),
    sale_price DECIMAL(15,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    length DECIMAL(10,2),
    wide DECIMAL(10,2),
    height DECIMAL(10,2),
    weight DECIMAL(10,2),
    tax_id BIGINT,
    views BIGINT NOT NULL DEFAULT 0,
    store_id BIGINT,
    created_by_id BIGINT NOT NULL DEFAULT 0,
    created_by_type VARCHAR(191) NOT NULL DEFAULT 'User',
    approved_by BIGINT DEFAULT 0,
    image VARCHAR(191),
    product_type VARCHAR(60) DEFAULT 'physical',
    barcode VARCHAR(150),
    cost_per_item DECIMAL(15,2),
    generate_license_code BOOLEAN NOT NULL DEFAULT FALSE,
    license_code_type VARCHAR(20) NOT NULL DEFAULT 'auto_generate',
    minimum_order_quantity INT DEFAULT 0,
    maximum_order_quantity INT DEFAULT 0,
    notify_attachment_updated BOOLEAN NOT NULL DEFAULT FALSE,
    specification_table_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES ec_brands(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_products_brand_status_variation ON ec_products(brand_id, status, is_variation, created_at);
CREATE INDEX IF NOT EXISTS idx_products_sale_type ON ec_products(sale_type);
CREATE INDEX IF NOT EXISTS idx_products_start_date ON ec_products(start_date);
CREATE INDEX IF NOT EXISTS idx_products_end_date ON ec_products(end_date);
CREATE INDEX IF NOT EXISTS idx_products_sale_price ON ec_products(sale_price);
CREATE INDEX IF NOT EXISTS idx_products_is_variation ON ec_products(is_variation);
CREATE INDEX IF NOT EXISTS idx_products_sku ON ec_products(sku);
CREATE INDEX IF NOT EXISTS idx_products_store_variation_status ON ec_products(store_id, is_variation, status);
CREATE INDEX IF NOT EXISTS idx_products_variation_name_id ON ec_products(is_variation, name, id);
CREATE INDEX IF NOT EXISTS idx_products_status ON ec_products(status);
CREATE INDEX IF NOT EXISTS idx_products_is_featured ON ec_products(is_featured);
