package com.chicamax.sentinella.alerts.domain.model.commands;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertKind;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateAlertCommand(
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        BigDecimal triggeredValue,
        AlertSeverity severity,
        String notificationChannels,
        UUID actorId,
        String actorRole,
        AlertKind alertKind,
        Long leadTimeMinutes,
        OffsetDateTime estimatedBreachAt
) {
    public CreateAlertCommand(
            UUID ruleId,
            UUID nodeId,
            String sensorType,
            BigDecimal triggeredValue,
            AlertSeverity severity,
            String notificationChannels,
            UUID actorId,
            String actorRole
    ) {
        this(ruleId, nodeId, sensorType, triggeredValue, severity, notificationChannels, actorId, actorRole,
                AlertKind.REACTIVE, null, null);
    }
}
