-- Notification Type Preferences table for granular preferences per notification type
CREATE TABLE IF NOT EXISTS notification_type_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT FALSE,
    push_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, notification_type)
);

CREATE INDEX idx_notification_type_prefs_user_type ON notification_type_preferences(user_id, notification_type);
CREATE INDEX idx_notification_type_prefs_user_id ON notification_type_preferences(user_id);

