CREATE SCHEMA IF NOT EXISTS blockchain;

-- node_id: sensor node (alertas/sensores) o tailing_dam_id (ROUND_SYNC) para scope RBAC
ALTER TABLE blockchain.ledger_records
    ADD COLUMN IF NOT EXISTS node_id UUID;

CREATE INDEX IF NOT EXISTS idx_ledger_records_node_id
    ON blockchain.ledger_records (node_id);
