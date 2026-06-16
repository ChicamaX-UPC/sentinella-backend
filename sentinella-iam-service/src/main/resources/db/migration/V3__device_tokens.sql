CREATE TABLE IF NOT EXISTS iam.device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES iam.users(id) ON DELETE CASCADE,
    token VARCHAR(512) NOT NULL,
    platform VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, token)
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user ON iam.device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_token ON iam.device_tokens(token);
