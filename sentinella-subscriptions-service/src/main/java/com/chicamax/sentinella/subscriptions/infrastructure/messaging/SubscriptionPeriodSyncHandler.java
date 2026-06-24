package com.chicamax.sentinella.subscriptions.infrastructure.messaging;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionPeriodSyncMessage;
import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPeriodSyncHandler {

    private final SubscriptionCommandService subscriptionCommandService;

    public SubscriptionPeriodSyncHandler(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @RabbitListener(queues = "subscription.period.sync.queue")
    public void onPeriodSync(SubscriptionPeriodSyncMessage message) {
        subscriptionCommandService.syncBillingPeriod(message);
    }
}
