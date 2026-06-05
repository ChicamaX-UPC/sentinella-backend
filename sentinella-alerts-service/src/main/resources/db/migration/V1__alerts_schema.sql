CREATE SCHEMA IF NOT EXISTS alerts;

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
