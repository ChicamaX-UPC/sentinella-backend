package com.chicamax.sentinella.alerts.domain.model.commands;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateAlertCommand(
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        BigDecimal triggeredValue,
        AlertSeverity severity,
        UUID actorId,
        String actorRole
) {
}
