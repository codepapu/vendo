-- Create discount_products relationship table
CREATE TABLE IF NOT EXISTS ec_discount_products (
    discount_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (discount_id, product_id),
    FOREIGN KEY (discount_id) REFERENCES ec_discounts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ec_products(id) ON DELETE CASCADE
);

-- Create discount_product_categories relationship table
CREATE TABLE IF NOT EXISTS ec_discount_product_categories (
    discount_id BIGINT NOT NULL,
    product_category_id BIGINT NOT NULL,
    PRIMARY KEY (discount_id, product_category_id),
    FOREIGN KEY (discount_id) REFERENCES ec_discounts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Create discount_customers relationship table
CREATE TABLE IF NOT EXISTS ec_discount_customers (
    discount_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    PRIMARY KEY (discount_id, customer_id),
    FOREIGN KEY (discount_id) REFERENCES ec_discounts(id) ON DELETE CASCADE
    -- Note: customer_id references ec_customers but table is in user-service
    -- Foreign key constraint can be added when both services share same database
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_discount_products_discount_id ON ec_discount_products(discount_id);
CREATE INDEX IF NOT EXISTS idx_discount_products_product_id ON ec_discount_products(product_id);
CREATE INDEX IF NOT EXISTS idx_discount_categories_discount_id ON ec_discount_product_categories(discount_id);
CREATE INDEX IF NOT EXISTS idx_discount_categories_category_id ON ec_discount_product_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_discount_customers_discount_id ON ec_discount_customers(discount_id);
CREATE INDEX IF NOT EXISTS idx_discount_customers_customer_id ON ec_discount_customers(customer_id);
