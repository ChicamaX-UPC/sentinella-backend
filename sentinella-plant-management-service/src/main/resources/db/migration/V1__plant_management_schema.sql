CREATE SCHEMA IF NOT EXISTS plant_management;

CREATE TABLE IF NOT EXISTS plant_management.relaves (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    tailing_dam_id UUID NOT NULL,
    capacity NUMERIC(14,2),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    address VARCHAR(300),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS plant_management.iot_nodes (
    id UUID PRIMARY KEY,
    external_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    tailing_dam_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    position_3d JSONB,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS plant_management.operators (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS plant_management.shift_reports (
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

CREATE TABLE IF NOT EXISTS plant_management.checklist_items (
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
    CONSTRAINT fk_checklist_shift_report FOREIGN KEY (round_id) REFERENCES plant_management.shift_reports(id)
);

CREATE TABLE IF NOT EXISTS plant_management.operator_assignments (
    id UUID PRIMARY KEY,
    operator_id UUID NOT NULL,
    tailing_dam_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
