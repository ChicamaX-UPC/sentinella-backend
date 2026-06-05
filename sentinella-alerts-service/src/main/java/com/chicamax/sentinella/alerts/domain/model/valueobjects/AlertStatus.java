package com.chicamax.sentinella.alerts.domain.model.valueobjects;

/** Ciclo canónico informe: RECEIVED → ACKNOWLEDGED → COMPLETED → CLOSED */
public enum AlertStatus {
    RECEIVED,
    ACKNOWLEDGED,
    COMPLETED,
    CLOSED
}
