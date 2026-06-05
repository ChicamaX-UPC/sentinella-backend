CREATE SCHEMA IF NOT EXISTS field_operations;

CREATE TABLE IF NOT EXISTS field_operations.inspection_rounds (
    id UUID PRIMARY KEY,
    operator_id UUID NOT NULL,
    tailing_dam_id UUID NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    offline_created BOOLEAN NOT NULL DEFAULT FALSE,
    synced_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS field_operations.checklist_items (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL,
    point_name VARCHAR(150) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    observations TEXT,
    photo_s3_key VARCHAR(500),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    completed_at TIMESTAMPTZ,
    is_anomaly BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_checklist_round FOREIGN KEY (round_id) REFERENCES field_operations.inspection_rounds(id)
);
