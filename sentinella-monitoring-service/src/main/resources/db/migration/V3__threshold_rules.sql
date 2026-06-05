CREATE TABLE IF NOT EXISTS monitoring.threshold_rules (
    id UUID PRIMARY KEY,
    node_id UUID NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    operator VARCHAR(10) NOT NULL,
    threshold_value NUMERIC(12,4) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    channels VARCHAR(255) NOT NULL DEFAULT 'APP',
    escalation_minutes INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by UUID,
    updated_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_threshold_rules_node ON monitoring.threshold_rules (node_id);
