ALTER TABLE profiles.user_profiles
    ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255);
