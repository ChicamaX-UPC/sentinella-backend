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
        AlertRuleSyncMessage message = new AlertRuleSyncMessage(
                rule.getId(),
                rule.getNodeId(),
                rule.getSensorType(),
                rule.getOperator().name(),
                rule.getThresholdValue(),
                rule.getSeverity().name(),
                rule.getChannels(),
                rule.getEscalationMinutes(),
                rule.isActive()
        );
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.ALERT_RULE_SYNC_ROUTING,
                message
        );
    }
}
