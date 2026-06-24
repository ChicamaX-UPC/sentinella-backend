package com.chicamax.sentinella.profiles.infrastructure.messaging;

import com.chicamax.sentinella.profiles.domain.services.ProfileCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionCancelledMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProfilePlanClearedConsumer {

    private final ProfileCommandService profileCommandService;

    public ProfilePlanClearedConsumer(ProfileCommandService profileCommandService) {
        this.profileCommandService = profileCommandService;
    }

    @RabbitListener(queues = "profile.plan.cleared.queue")
    public void onPlanCleared(SubscriptionCancelledMessage message) {
        profileCommandService.applySubscriptionCancelled(message);
    }
}
