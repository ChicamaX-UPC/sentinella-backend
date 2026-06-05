package com.chicamax.sentinella.subscriptions.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.subscriptions.domain.model.events.SubscriptionActivatedEvent;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionActivatedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionActivatedRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SubscriptionActivatedRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(SubscriptionActivatedEvent event) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.SUBSCRIPTION_ACTIVATED_ROUTING,
                new SubscriptionActivatedMessage(
                        event.userId(),
                        event.subscriptionId(),
                        event.planType(),
                        event.sensorLimit()
                )
        );
    }
}
