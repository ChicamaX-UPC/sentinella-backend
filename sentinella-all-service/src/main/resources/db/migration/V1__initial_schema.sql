CREATE SCHEMA IF NOT EXISTS iam;
CREATE SCHEMA IF NOT EXISTS monitoring;
CREATE SCHEMA IF NOT EXISTS alerts;
CREATE SCHEMA IF NOT EXISTS field_operations;
CREATE SCHEMA IF NOT EXISTS node_admin;
CREATE SCHEMA IF NOT EXISTS reports;

CREATE TABLE IF NOT EXISTS iam.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tailing_dam_ids UUID[],
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMPTZ,
    failed_attempts SMALLINT NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS monitoring.sensor_nodes (
    id UUID PRIMARY KEY,
    external_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    tailing_dam_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    position_3d JSONB,
    status VARCHAR(20) NOT NULL,
    last_seen TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS monitoring.sensor_readings (
    id UUID NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    node_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    value NUMERIC(12,4) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    raw_payload JSONB,
    PRIMARY KEY (id, timestamp)
);

CREATE INDEX IF NOT EXISTS idx_sensor_readings_node_timestamp
    ON monitoring.sensor_readings (node_id, timestamp DESC);

CREATE TABLE IF NOT EXISTS alerts.alert_rules (
    id UUID PRIMARY KEY,
    node_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    operator VARCHAR(10) NOT NULL,
    threshold_value NUMERIC(12,4) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    channels VARCHAR(255) NOT NULL,
    escalation_minutes INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by UUID,
    updated_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS alerts.alerts (
    id UUID PRIMARY KEY,
    rule_id UUID,
    node_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    triggered_value NUMERIC(12,4) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    acknowledged_by UUID,
    acknowledged_at TIMESTAMPTZ,
    assigned_to UUID,
    closed_by UUID,
    closed_at TIMESTAMPTZ,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alert_rule FOREIGN KEY (rule_id) REFERENCES alerts.alert_rules(id)
);

CREATE TABLE IF NOT EXISTS alerts.alert_audit_log (
    id UUID PRIMARY KEY,
    alert_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id UUID NOT NULL,
    actor_role VARCHAR(50) NOT NULL,
    notes TEXT,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alert_audit_alert FOREIGN KEY (alert_id) REFERENCES alerts.alerts(id)
);

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

CREATE TABLE IF NOT EXISTS node_admin.iot_nodes (
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

CREATE TABLE IF NOT EXISTS reports.reports (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    format VARCHAR(20) NOT NULL,
    tailing_dam_id UUID,
    from_date TIMESTAMPTZ NOT NULL,
    to_date TIMESTAMPTZ NOT NULL,
    generated_by UUID NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
