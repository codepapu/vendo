-- Create currencies table
CREATE TABLE IF NOT EXISTS ec_currencies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(191) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    is_prefix_symbol BOOLEAN NOT NULL DEFAULT FALSE,
    decimals INT NOT NULL DEFAULT 0,
    exchange_rate DECIMAL(15,8) NOT NULL DEFAULT 1.00000000,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    order INT NOT NULL DEFAULT 0,
    status VARCHAR(60) NOT NULL DEFAULT 'published',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_currencies_status ON ec_currencies(status);
CREATE INDEX IF NOT EXISTS idx_currencies_is_default ON ec_currencies(is_default);
