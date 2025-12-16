-- Create customer addresses table
CREATE TABLE IF NOT EXISTS ec_customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    name VARCHAR(60) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(191),
    country VARCHAR(120),
    state VARCHAR(120),
    city VARCHAR(120),
    address VARCHAR(191),
    zip_code VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES ec_customers(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_id ON ec_customer_addresses(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_addresses_is_default ON ec_customer_addresses(customer_id, is_default);
