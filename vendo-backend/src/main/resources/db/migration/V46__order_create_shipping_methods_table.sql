-- Create shipping_methods table
CREATE TABLE IF NOT EXISTS shipping_methods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    carrier VARCHAR(100),
    base_cost DECIMAL(10,2) NOT NULL,
    cost_per_kg DECIMAL(10,2),
    estimated_days INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_shipping_methods_code ON shipping_methods(code);
CREATE INDEX IF NOT EXISTS idx_shipping_methods_is_active ON shipping_methods(is_active);

-- Insert default shipping methods
INSERT INTO shipping_methods (name, code, description, carrier, base_cost, cost_per_kg, estimated_days, is_active)
VALUES
    ('Standard Shipping', 'STANDARD', 'Standard shipping (5-7 business days)', 'Standard Carrier', 5.99, 0.50, 7, TRUE),
    ('Express Shipping', 'EXPRESS', 'Express shipping (2-3 business days)', 'Express Carrier', 12.99, 1.00, 3, TRUE),
    ('Overnight Shipping', 'OVERNIGHT', 'Overnight shipping (1 business day)', 'Overnight Carrier', 24.99, 2.00, 1, TRUE),
    ('Economy Shipping', 'ECONOMY', 'Economy shipping (7-10 business days)', 'Economy Carrier', 3.99, 0.25, 10, TRUE)
ON CONFLICT (code) DO NOTHING;

