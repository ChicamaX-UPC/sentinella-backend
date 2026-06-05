package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.events.AlertClosedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertStatusUpdatedEvent;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertAcknowledgedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertClosedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertCreatedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlertEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AlertEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void onAlertCreated(AlertCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.ALERT_CREATED_ROUTING,
                new AlertCreatedMessage(event.alertId(), event.nodeId(), event.severity())
        );
    }

    @EventListener
    public void onAlertClosed(AlertClosedEvent event) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.ALERT_CLOSED_ROUTING,
                new AlertClosedMessage(event.alertId(), event.nodeId())
        );
    }

    @EventListener
    public void onAlertStatusUpdated(AlertStatusUpdatedEvent event) {
        if (AlertStatus.ACKNOWLEDGED.name().equals(event.status())) {
            rabbitTemplate.convertAndSend(
                    SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                    SentinellaMessagingConstants.ALERT_ACKNOWLEDGED_ROUTING,
                    new AlertAcknowledgedMessage(event.alertId(), event.nodeId())
            );
        }
    }
}
