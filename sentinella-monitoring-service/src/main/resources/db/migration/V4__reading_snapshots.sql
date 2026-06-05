CREATE TABLE IF NOT EXISTS monitoring.reading_snapshots (
    id UUID PRIMARY KEY,
    node_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    bucket_start TIMESTAMPTZ NOT NULL,
    avg_value NUMERIC(12,4) NOT NULL,
    min_value NUMERIC(12,4) NOT NULL,
    max_value NUMERIC(12,4) NOT NULL,
    sample_count INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reading_snapshots_node_bucket
    ON monitoring.reading_snapshots (node_id, bucket_start DESC);
