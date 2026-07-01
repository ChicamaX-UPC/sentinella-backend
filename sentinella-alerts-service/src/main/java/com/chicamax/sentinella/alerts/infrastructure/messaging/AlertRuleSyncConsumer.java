package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertRuleSyncMessage;
import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Mantiene la réplica {@code alerts.alert_rules} a partir de las reglas que publica Monitoring
 * ({@code alert.rule.sync}). Upsert idempotente por id: así el FK {@code fk_alert_rule} de las
 * alertas queda satisfecho. Traduce el vocabulario de severidad de Monitoring (que incluye HIGH y
 * MEDIUM) al de Alerts ({@link AlertSeverity}).
 */
@Component
public class AlertRuleSyncConsumer {

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleSyncConsumer(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @RabbitListener(queues = "alert.rule.sync.queue")
    public void onAlertRuleSync(AlertRuleSyncMessage message) {
        AlertRuleOperator operator = toOperator(message.operator());
        AlertSeverity severity = toSeverity(message.severity());
        AlertChannel[] channels = toChannels(message.channels());

        AlertRule existing = alertRuleRepository.findById(message.ruleId()).orElse(null);
        if (existing == null) {
            alertRuleRepository.save(new AlertRule(
                    message.ruleId(),
                    message.nodeId(),
                    message.sensorType(),
                    operator,
                    message.thresholdValue(),
                    severity,
                    channels,
                    message.escalationMinutes(),
                    null
            ));
        } else {
            existing.update(
                    message.sensorType(),
                    operator,
                    message.thresholdValue(),
                    severity,
                    channels,
                    message.escalationMinutes(),
                    message.active(),
                    null
            );
            alertRuleRepository.save(existing);
        }
    }

    private static AlertRuleOperator toOperator(String raw) {
        try {
            return AlertRuleOperator.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            return AlertRuleOperator.GTE;
        }
    }

    private static AlertSeverity toSeverity(String raw) {
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

    private static AlertChannel[] toChannels(String raw) {
        if (raw == null || raw.isBlank()) {
            return new AlertChannel[] {AlertChannel.APP};
        }
        List<AlertChannel> channels = new ArrayList<>();
        for (String part : raw.split(",")) {
            try {
                channels.add(AlertChannel.valueOf(part.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // Canal desconocido en el vocabulario de Alerts: se omite.
            }
        }
        return channels.isEmpty()
                ? new AlertChannel[] {AlertChannel.APP}
                : channels.toArray(new AlertChannel[0]);
    }
}
