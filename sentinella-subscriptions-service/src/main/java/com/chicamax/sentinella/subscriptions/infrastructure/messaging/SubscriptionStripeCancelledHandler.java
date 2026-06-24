package com.chicamax.sentinella.subscriptions.infrastructure.messaging;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionCancelledMessage;
import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionStripeCancelledHandler {

    private final SubscriptionCommandService subscriptionCommandService;

    public SubscriptionStripeCancelledHandler(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @RabbitListener(queues = "subscription.stripe.cancelled.queue")
    public void onStripeCancelled(SubscriptionCancelledMessage message) {
        subscriptionCommandService.cancelByStripeSubscriptionId(message.stripeSubscriptionId());
    }
}
