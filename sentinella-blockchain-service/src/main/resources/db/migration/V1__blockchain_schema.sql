CREATE SCHEMA IF NOT EXISTS blockchain;

CREATE TABLE IF NOT EXISTS blockchain.ledger_records (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    fabric_tx_id VARCHAR(128),
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
