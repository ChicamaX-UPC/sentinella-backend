package com.chicamax.sentinella.subscriptions.infrastructure.messaging;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.PaymentCompletedMessage;
import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedEventHandler {

    private final SubscriptionCommandService subscriptionCommandService;

    public PaymentCompletedEventHandler(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @RabbitListener(queues = "payment.completed.queue")
    public void onPaymentCompleted(PaymentCompletedMessage message) {
        subscriptionCommandService.activateFromPayment(message);
    }
}
