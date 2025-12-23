-- Create cart table (Laravel-style cart storage)
-- Note: In Spring Boot, cart is typically stored in Redis for better performance
-- This table is for persistent cart storage if needed
CREATE TABLE IF NOT EXISTS ec_cart (
    identifier VARCHAR(60) NOT NULL,
    instance VARCHAR(60) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (identifier, instance)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_cart_identifier ON ec_cart(identifier);
CREATE INDEX IF NOT EXISTS idx_cart_instance ON ec_cart(instance);
