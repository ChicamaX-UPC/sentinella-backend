ALTER TABLE plant_management.relaves
    ADD COLUMN IF NOT EXISTS organization_id UUID;

UPDATE plant_management.relaves
SET organization_id = 'e0000001-0001-4001-8001-000000000001'
WHERE organization_id IS NULL;

ALTER TABLE plant_management.relaves
    ALTER COLUMN organization_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_relaves_organization
    ON plant_management.relaves (organization_id);
