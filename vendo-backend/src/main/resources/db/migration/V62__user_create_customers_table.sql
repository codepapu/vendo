-- Create customers table (e-commerce customers - can be linked to users)
CREATE TABLE IF NOT EXISTS ec_customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(191) NOT NULL,
    email VARCHAR(191) UNIQUE,
    password VARCHAR(191) NOT NULL,
    avatar VARCHAR(191),
    dob DATE,
    phone VARCHAR(20),
    remember_token VARCHAR(100),
    confirmed_at TIMESTAMP,
    email_verify_token VARCHAR(120),
    is_vendor BOOLEAN NOT NULL DEFAULT FALSE,
    vendor_verified_at TIMESTAMP,
    status VARCHAR(60) NOT NULL DEFAULT 'activated',
    block_reason VARCHAR(400),
    private_notes TEXT,
    stripe_account_id VARCHAR(191),
    stripe_account_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_customers_user_id ON ec_customers(user_id);
CREATE INDEX IF NOT EXISTS idx_customers_email ON ec_customers(email);
CREATE INDEX IF NOT EXISTS idx_customers_status ON ec_customers(status);
