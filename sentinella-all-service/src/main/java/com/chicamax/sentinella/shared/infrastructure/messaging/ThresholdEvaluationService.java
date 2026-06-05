package com.chicamax.sentinella.shared.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.ThresholdExceededMessage;
import java.math.BigDecimal;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ThresholdEvaluationService {

    private final AlertRuleRepository alertRuleRepository;
    private final RabbitTemplate rabbitTemplate;

    public ThresholdEvaluationService(AlertRuleRepository alertRuleRepository, RabbitTemplate rabbitTemplate) {
        this.alertRuleRepository = alertRuleRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void evaluateInternal(SensorReadingReceivedMessage message) {
        for (AlertRule rule : alertRuleRepository.findAll()) {
            if (!rule.isActive()) {
                continue;
            }
            if (!rule.getNodeId().equals(message.nodeId())) {
                continue;
            }
            if (!sameSensorType(rule.getSensorType(), message.sensorType())) {
                continue;
            }
            if (isExceeded(message.value(), rule.getThresholdValue(), rule.getOperator())) {
                rabbitTemplate.convertAndSend(
                        SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                        SentinellaMessagingConstants.THRESHOLD_EXCEEDED_ROUTING,
                        new ThresholdExceededMessage(
                                rule.getId(),
                                message.nodeId(),
                                message.sensorType(),
                                message.value(),
                                rule.getSeverity().name()
                        )
                );
            }
        }
    }

    private static boolean sameSensorType(String ruleSt, String messageSt) {
        return norm(ruleSt).equals(norm(messageSt));
    }

    private static String norm(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase().replace('-', '_');
    }

    private static boolean isExceeded(BigDecimal current, BigDecimal threshold, AlertRuleOperator operator) {
        int cmp = current.compareTo(threshold);
        return switch (operator) {
            case GT -> cmp > 0;
            case LT -> cmp < 0;
            case GTE -> cmp >= 0;
            case LTE -> cmp <= 0;
        };
    }
}
