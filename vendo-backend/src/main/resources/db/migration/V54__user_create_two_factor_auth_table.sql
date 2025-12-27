-- Create two_factor_auth table
CREATE TABLE IF NOT EXISTS two_factor_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    secret_key VARCHAR(255),
    backup_codes TEXT,
    qr_code_url TEXT,
    last_used_backup_code TIMESTAMP,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_two_factor_auth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_2fa_user_id ON two_factor_auth(user_id);
CREATE INDEX IF NOT EXISTS idx_2fa_enabled ON two_factor_auth(enabled);

