package com.chicamax.sentinella.fieldoperations.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.RoundSyncedRabbitMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoundSyncedRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RoundSyncedRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(RoundSyncedRabbitMessage message) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.ROUND_SYNCED_ROUTING,
                message
        );
    }
}
