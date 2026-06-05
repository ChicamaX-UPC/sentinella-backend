package com.chicamax.sentinella.plantmanagement.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorRegisteredMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SensorRegisteredRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SensorRegisteredRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(SensorRegisteredMessage message) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SENSOR_REGISTERED_ROUTING,
                message
        );
    }
}
