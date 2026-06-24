package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.infrastructure.cache.ThresholdRuleCache;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertTriggeredMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.ThresholdExceededMessage;
import java.math.BigDecimal;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ThresholdEvaluationService {

    private final ThresholdRuleCache thresholdRuleCache;
    private final RabbitTemplate rabbitTemplate;

    public ThresholdEvaluationService(ThresholdRuleCache thresholdRuleCache, RabbitTemplate rabbitTemplate) {
        this.thresholdRuleCache = thresholdRuleCache;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void evaluate(SensorReadingReceivedMessage message) {
        for (ThresholdRule rule : thresholdRuleCache.activeRulesForNode(message.nodeId())) {
            if (!sameSensorType(rule.getSensorType(), message.sensorType())) {
                continue;
            }
            if (isExceeded(message.value(), rule.getThresholdValue(), rule.getOperator())) {
                var exceeded = new ThresholdExceededMessage(
                        rule.getId(),
                        message.nodeId(),
                        message.sensorType(),
                        message.value(),
                                rule.getSeverity().name()
                        );
                rabbitTemplate.convertAndSend(
                        SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                        SentinellaMessagingConstants.THRESHOLD_EXCEEDED_ROUTING,
                        exceeded
                );
                rabbitTemplate.convertAndSend(
                        SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                        SentinellaMessagingConstants.ALERT_TRIGGERED_ROUTING,
                        AlertTriggeredMessage.fromThreshold(exceeded, rule.getChannels())
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

    private static boolean isExceeded(BigDecimal current, BigDecimal threshold, ThresholdRuleOperator operator) {
        int cmp = current.compareTo(threshold);
        return switch (operator) {
            case GT -> cmp > 0;
            case LT -> cmp < 0;
            case GTE -> cmp >= 0;
            case LTE -> cmp <= 0;
        };
    }
}
