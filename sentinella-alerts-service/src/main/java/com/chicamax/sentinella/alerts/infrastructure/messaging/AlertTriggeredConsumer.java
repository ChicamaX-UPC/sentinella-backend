package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertTriggeredMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertTriggeredConsumer {

    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SYSTEM_ROLE = "SYSTEM_ADMIN";

    private final AlertCommandService alertCommandService;

    public AlertTriggeredConsumer(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @RabbitListener(queues = "alert.triggered.queue")
    public void onAlertTriggered(AlertTriggeredMessage message) {
        alertCommandService.create(new CreateAlertCommand(
                message.ruleId(),
                message.nodeId(),
                message.sensorType(),
                message.value(),
                toAlertSeverity(message.severity()),
                message.channels(),
                SYSTEM_ACTOR_ID,
                SYSTEM_ROLE
        ));
    }

    /**
     * Mapea el vocabulario de severidad de monitoring ({@code ThresholdSeverity}: INFO, WARNING,
     * CRITICAL, HIGH, MEDIUM) al de alerts ({@link AlertSeverity}: INFO, WARNING, CRITICAL).
     * Tolerante a nulos/desconocidos (cae a WARNING) para no rechazar telemetría a la DLQ.
     */
    private static AlertSeverity toAlertSeverity(String raw) {
        if (raw == null || raw.isBlank()) {
            return AlertSeverity.WARNING;
        }
        return switch (raw.trim().toUpperCase()) {
            case "INFO", "LOW" -> AlertSeverity.INFO;
            case "WARNING", "MEDIUM", "MED" -> AlertSeverity.WARNING;
            case "CRITICAL", "HIGH", "SEVERE" -> AlertSeverity.CRITICAL;
            default -> AlertSeverity.WARNING;
        };
    }
}
