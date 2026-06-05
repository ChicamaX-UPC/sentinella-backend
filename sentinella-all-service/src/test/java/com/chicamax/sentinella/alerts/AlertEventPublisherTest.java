package com.chicamax.sentinella.alerts;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.chicamax.sentinella.alerts.domain.model.events.AlertClosedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.alerts.infrastructure.messaging.AlertEventPublisher;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertClosedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertCreatedMessage;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class AlertEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishCreatedAndClosedEventsToBroker() {
        AlertEventPublisher publisher = new AlertEventPublisher(rabbitTemplate);
        UUID alertId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        publisher.onAlertCreated(new AlertCreatedEvent(alertId, nodeId, "CRITICAL"));
        publisher.onAlertClosed(new AlertClosedEvent(alertId, nodeId));

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(SentinellaMessagingConstants.SENTINELLA_EXCHANGE),
                eq(SentinellaMessagingConstants.ALERT_CREATED_ROUTING),
                isA(AlertCreatedMessage.class)
        );
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(SentinellaMessagingConstants.SENTINELLA_EXCHANGE),
                eq(SentinellaMessagingConstants.ALERT_CLOSED_ROUTING),
                isA(AlertClosedMessage.class)
        );
    }
}
