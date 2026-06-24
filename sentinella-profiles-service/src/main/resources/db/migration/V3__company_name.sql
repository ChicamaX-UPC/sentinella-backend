ALTER TABLE profiles.user_profiles
    ADD COLUMN IF NOT EXISTS company_name VARCHAR(200);
