ALTER TABLE blockchain.ledger_records
    ADD COLUMN IF NOT EXISTS related_entity_id UUID;

CREATE INDEX IF NOT EXISTS idx_ledger_records_related_entity_id
    ON blockchain.ledger_records (related_entity_id);
