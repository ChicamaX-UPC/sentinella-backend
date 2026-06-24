ALTER TABLE alerts.alerts
    ADD COLUMN IF NOT EXISTS alert_kind VARCHAR(20) NOT NULL DEFAULT 'REACTIVE',
    ADD COLUMN IF NOT EXISTS lead_time_minutes BIGINT,
    ADD COLUMN IF NOT EXISTS estimated_breach_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_alerts_predictive_open
    ON alerts.alerts (alert_kind, status)
    WHERE alert_kind = 'PREDICTIVE' AND status = 'RECEIVED';
