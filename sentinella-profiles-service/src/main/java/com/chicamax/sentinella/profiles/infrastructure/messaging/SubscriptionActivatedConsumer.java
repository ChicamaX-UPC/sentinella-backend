package com.chicamax.sentinella.profiles.infrastructure.messaging;

import com.chicamax.sentinella.profiles.domain.services.ProfileCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionActivatedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionActivatedConsumer {

    private final ProfileCommandService profileCommandService;

    public SubscriptionActivatedConsumer(ProfileCommandService profileCommandService) {
        this.profileCommandService = profileCommandService;
    }

    @RabbitListener(queues = "subscription.activated.queue")
    public void onSubscriptionActivated(SubscriptionActivatedMessage message) {
        profileCommandService.applySubscriptionActivated(message);
    }
}
