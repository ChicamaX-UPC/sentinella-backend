package com.chicamax.sentinella.shared;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.ThresholdEvaluationService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.ThresholdExceededMessage;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class ThresholdEvaluationServiceTest {

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishThresholdExceededWhenRuleMatches() {
        UUID nodeId = UUID.randomUUID();
        AlertRule rule = new AlertRule(
                UUID.randomUUID(),
                nodeId,
                "water_level",
                AlertRuleOperator.GT,
                BigDecimal.valueOf(10),
                AlertSeverity.CRITICAL,
                new AlertChannel[] {AlertChannel.APP},
                30,
                UUID.randomUUID()
        );
        when(alertRuleRepository.findAll()).thenReturn(List.of(rule));

        ThresholdEvaluationService service = new ThresholdEvaluationService(alertRuleRepository, rabbitTemplate);
        service.evaluateInternal(new SensorReadingReceivedMessage(
                nodeId,
                OffsetDateTime.now(),
                "water_level",
                BigDecimal.valueOf(12),
                "meters",
                "CRITICAL",
                null
        ));

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(SentinellaMessagingConstants.SENTINELLA_EXCHANGE),
                eq(SentinellaMessagingConstants.THRESHOLD_EXCEEDED_ROUTING),
                isA(ThresholdExceededMessage.class)
        );
    }
}
