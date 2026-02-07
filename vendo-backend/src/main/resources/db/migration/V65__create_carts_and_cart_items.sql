-- Create carts and cart_items tables for application cart flow (ec_cart is legacy Laravel-style).
-- Schema matches Cart and CartItem JPA entities.

CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    business_account_id BIGINT,
    session_id VARCHAR(255),
    cart_type VARCHAR(20) NOT NULL DEFAULT 'B2C',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_carts_business_account FOREIGN KEY (business_account_id) REFERENCES business_accounts(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_carts_user_id ON carts(user_id);
CREATE INDEX IF NOT EXISTS idx_carts_business_account_id ON carts(business_account_id);
CREATE INDEX IF NOT EXISTS idx_carts_session_id ON carts(session_id);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT,
    retailer_id BIGINT,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE SET NULL,
    CONSTRAINT fk_cart_items_retailer FOREIGN KEY (retailer_id) REFERENCES retailers(id) ON DELETE SET NULL,
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_unit_price CHECK (unit_price >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_cart_items_cart_product_retailer ON cart_items(cart_id, product_id, COALESCE(retailer_id, -1));
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_retailer_id ON cart_items(retailer_id);
