-- Create review replies table
CREATE TABLE IF NOT EXISTS ec_review_replies (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES ec_reviews(id) ON DELETE CASCADE,
    UNIQUE (review_id, user_id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_review_replies_review_id ON ec_review_replies(review_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_user_id ON ec_review_replies(user_id);
