ALTER TABLE subscriptions.subscriptions
    ADD COLUMN IF NOT EXISTS current_period_end TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS billing_email VARCHAR(320),
    ADD COLUMN IF NOT EXISTS renewal_warning_sent_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_dunning_attempt_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS dunning_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_invoice_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_subscriptions_period_end
    ON subscriptions.subscriptions (current_period_end)
    WHERE status IN ('ACTIVE', 'PAST_DUE');
