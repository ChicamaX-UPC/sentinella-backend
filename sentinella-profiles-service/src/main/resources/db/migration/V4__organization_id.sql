ALTER TABLE profiles.user_profiles
    ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE profiles.user_profiles
SET organization_id = 'e0000001-0001-4001-8001-000000000001'
WHERE organization_id IS NULL;
