package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.monitoring.domain.model.events.SensorReadingRegisteredEvent;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SensorReadingMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public SensorReadingMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPersistedReading(SensorReadingRegisteredEvent event) {
        SensorReadingReceivedMessage payload = new SensorReadingReceivedMessage(
                event.nodeId(),
                event.timestamp(),
                event.sensorType(),
                event.value(),
                event.unit(),
                event.status(),
                null
        );
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SENSOR_READING_PERSISTED_ROUTING,
                payload
        );
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SENSOR_READING_REALTIME_ROUTING,
                payload
        );
    }
}
