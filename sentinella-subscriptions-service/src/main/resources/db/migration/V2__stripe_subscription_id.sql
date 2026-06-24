ALTER TABLE subscriptions.subscriptions
    ADD COLUMN IF NOT EXISTS stripe_subscription_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS idx_subscriptions_stripe_subscription_id
    ON subscriptions.subscriptions (stripe_subscription_id)
    WHERE stripe_subscription_id IS NOT NULL;
