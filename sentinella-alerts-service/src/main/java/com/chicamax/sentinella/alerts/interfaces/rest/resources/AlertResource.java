package com.chicamax.sentinella.alerts.interfaces.rest.resources;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertKind;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertResource(
        UUID id,
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        BigDecimal triggeredValue,
        AlertSeverity severity,
        AlertStatus status,
        UUID acknowledgedBy,
        OffsetDateTime acknowledgedAt,
        UUID assignedTo,
        UUID closedBy,
        OffsetDateTime closedAt,
        String resolutionNotes,
        AlertKind alertKind,
        Long leadTimeMinutes,
        OffsetDateTime estimatedBreachAt
) {
}
