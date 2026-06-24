package com.chicamax.sentinella.subscriptions.infrastructure.messaging;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionPaymentFailedMessage;
import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPaymentFailedHandler {

    private final SubscriptionCommandService subscriptionCommandService;

    public SubscriptionPaymentFailedHandler(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @RabbitListener(queues = "subscription.payment.failed.queue")
    public void onPaymentFailed(SubscriptionPaymentFailedMessage message) {
        subscriptionCommandService.recordPaymentFailed(message);
    }
}
