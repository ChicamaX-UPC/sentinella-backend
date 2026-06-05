package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

/** Evento de dominio Monitoring → Alerts (umbrales excedidos). */
public record AlertTriggeredMessage(
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        BigDecimal value,
        String severity,
        String channels
) {
    public AlertTriggeredMessage(
            UUID ruleId,
            UUID nodeId,
            String sensorType,
            BigDecimal value,
            String severity
    ) {
        this(ruleId, nodeId, sensorType, value, severity, "APP");
    }

    public static AlertTriggeredMessage fromThreshold(ThresholdExceededMessage message, String channels) {
        return new AlertTriggeredMessage(
                message.ruleId(),
                message.nodeId(),
                message.sensorType(),
                message.value(),
                message.severity(),
                channels != null && !channels.isBlank() ? channels : "APP"
        );
    }
}
