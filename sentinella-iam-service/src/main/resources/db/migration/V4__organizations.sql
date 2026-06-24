CREATE TABLE IF NOT EXISTS iam.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE iam.users
    ADD COLUMN IF NOT EXISTS organization_id UUID;

INSERT INTO iam.organizations (id, name)
VALUES ('e0000001-0001-4001-8001-000000000001', 'Sentinella Demo')
ON CONFLICT (id) DO NOTHING;

UPDATE iam.users
SET organization_id = 'e0000001-0001-4001-8001-000000000001'
WHERE organization_id IS NULL
  AND email LIKE '%@sentinella.demo';

DO $$
DECLARE
    u RECORD;
    org_id UUID;
BEGIN
    FOR u IN
        SELECT id, email, full_name
        FROM iam.users
        WHERE organization_id IS NULL
    LOOP
        org_id := gen_random_uuid();
        INSERT INTO iam.organizations (id, name)
        VALUES (org_id, COALESCE(NULLIF(TRIM(u.full_name), ''), split_part(u.email, '@', 1)));
        UPDATE iam.users SET organization_id = org_id WHERE id = u.id;
    END LOOP;
END $$;

ALTER TABLE iam.users
    ALTER COLUMN organization_id SET NOT NULL;

ALTER TABLE iam.users
    ADD CONSTRAINT fk_users_organization
        FOREIGN KEY (organization_id) REFERENCES iam.organizations (id);

CREATE INDEX IF NOT EXISTS idx_users_organization ON iam.users (organization_id);
