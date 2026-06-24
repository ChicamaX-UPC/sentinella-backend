package com.chicamax.sentinella.plantmanagement.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.RelaveCreatedMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RelaveCreatedRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RelaveCreatedRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(UUID organizationId, UUID createdByUserId, UUID tailingDamId) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.RELAVE_CREATED_ROUTING,
                new RelaveCreatedMessage(organizationId, createdByUserId, tailingDamId)
        );
    }
}
