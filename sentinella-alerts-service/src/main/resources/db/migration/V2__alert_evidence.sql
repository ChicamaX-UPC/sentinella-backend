CREATE TABLE IF NOT EXISTS alerts.alert_evidence (
    id UUID PRIMARY KEY,
    alert_id UUID NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(100),
    uploaded_by UUID,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alert_evidence_alert FOREIGN KEY (alert_id) REFERENCES alerts.alerts(id)
);
