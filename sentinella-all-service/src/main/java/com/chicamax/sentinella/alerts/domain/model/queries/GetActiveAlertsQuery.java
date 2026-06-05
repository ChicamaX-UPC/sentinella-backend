package com.chicamax.sentinella.alerts.domain.model.queries;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import java.util.UUID;

public record GetActiveAlertsQuery(AlertStatus status, AlertSeverity severity, UUID nodeId) {
}
