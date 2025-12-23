-- Create shipping table
CREATE TABLE IF NOT EXISTS ec_shipping (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(191) NOT NULL,
    country VARCHAR(120),
    state VARCHAR(120),
    city VARCHAR(120),
    zip_code VARCHAR(20),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_shipping_country ON ec_shipping(country);
CREATE INDEX IF NOT EXISTS idx_shipping_is_enabled ON ec_shipping(is_enabled);
