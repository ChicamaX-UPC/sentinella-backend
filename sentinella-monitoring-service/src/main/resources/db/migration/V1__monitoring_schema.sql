CREATE SCHEMA IF NOT EXISTS monitoring;

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
