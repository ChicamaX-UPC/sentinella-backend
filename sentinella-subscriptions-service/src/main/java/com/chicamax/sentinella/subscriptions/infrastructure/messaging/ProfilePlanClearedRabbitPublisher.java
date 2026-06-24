package com.chicamax.sentinella.subscriptions.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionCancelledMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProfilePlanClearedRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ProfilePlanClearedRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(UUID userId, UUID subscriptionId, String stripeSubscriptionId) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.PROFILE_PLAN_CLEARED_ROUTING,
                new SubscriptionCancelledMessage(userId, subscriptionId, stripeSubscriptionId)
        );
    }
}
