-- Create brands table
CREATE TABLE IF NOT EXISTS ec_brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    description TEXT,
    website VARCHAR(191),
    logo VARCHAR(191),
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    order INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_brands_status ON ec_brands(status);
CREATE INDEX IF NOT EXISTS idx_brands_order ON ec_brands(order);
