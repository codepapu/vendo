-- Create invoices table
CREATE TABLE IF NOT EXISTS ec_invoices (
    id BIGSERIAL PRIMARY KEY,
    reference_type VARCHAR(191) NOT NULL,
    reference_id BIGINT NOT NULL,
    code VARCHAR(191) NOT NULL UNIQUE,
    customer_name VARCHAR(191),
    company_name VARCHAR(191),
    company_logo VARCHAR(191),
    customer_email VARCHAR(191),
    customer_phone VARCHAR(191),
    customer_address VARCHAR(191),
    customer_country VARCHAR(191),
    customer_state VARCHAR(191),
    customer_city VARCHAR(191),
    customer_zip_code VARCHAR(191),
    customer_address_line VARCHAR(191),
    customer_tax_id VARCHAR(191),
    sub_total DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0.00,
    shipping_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payment_fee DECIMAL(15,2) DEFAULT 0.00,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    shipping_option VARCHAR(60),
    shipping_method VARCHAR(60) NOT NULL DEFAULT 'default',
    coupon_code VARCHAR(120),
    discount_description VARCHAR(191),
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    payment_id BIGINT,
    status VARCHAR(191) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_invoices_code ON ec_invoices(code);
CREATE INDEX IF NOT EXISTS idx_invoices_reference ON ec_invoices(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON ec_invoices(status);
