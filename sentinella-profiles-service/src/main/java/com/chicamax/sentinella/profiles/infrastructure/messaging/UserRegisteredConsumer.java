package com.chicamax.sentinella.profiles.infrastructure.messaging;

import com.chicamax.sentinella.profiles.domain.services.ProfileCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.UserRegisteredMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredConsumer {

    private final ProfileCommandService profileCommandService;

    public UserRegisteredConsumer(ProfileCommandService profileCommandService) {
        this.profileCommandService = profileCommandService;
    }

    @RabbitListener(queues = "user.registered.queue")
    public void onUserRegistered(UserRegisteredMessage message) {
        profileCommandService.createFromRegistration(message);
    }
}
