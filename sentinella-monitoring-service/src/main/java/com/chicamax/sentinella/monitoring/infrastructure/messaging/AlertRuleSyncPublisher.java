package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertRuleSyncMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica cada regla de umbral hacia Alerts ({@code alert.rule.sync}) para que mantenga su réplica
 * {@code alert_rules} y pueda satisfacer el FK de {@code alerts.alerts}.
 */
@Component
public class AlertRuleSyncPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AlertRuleSyncPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ThresholdRule rule) {
        send(toMessage(rule));
    }

    /** Marca la réplica en Alerts como inactiva antes de borrar la regla en Monitoring. */
    public void publishDeactivated(ThresholdRule rule) {
        send(toMessage(rule, false));
    }

    private void send(AlertRuleSyncMessage message) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.ALERT_RULE_SYNC_ROUTING,
                message
        );
    }

    private static AlertRuleSyncMessage toMessage(ThresholdRule rule) {
        return toMessage(rule, rule.isActive());
    }

    private static AlertRuleSyncMessage toMessage(ThresholdRule rule, boolean active) {
        return new AlertRuleSyncMessage(
                rule.getId(),
                rule.getNodeId(),
                rule.getSensorType(),
                rule.getOperator().name(),
                rule.getThresholdValue(),
                rule.getSeverity().name(),
                rule.getChannels(),
                rule.getEscalationMinutes(),
                active
        );
    }
}
