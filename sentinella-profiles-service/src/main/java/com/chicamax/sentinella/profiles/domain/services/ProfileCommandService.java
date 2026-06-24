package com.chicamax.sentinella.profiles.domain.services;

import com.chicamax.sentinella.profiles.domain.model.aggregates.UserProfile;
import com.chicamax.sentinella.profiles.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.profiles.domain.model.valueobjects.ActivePlanReference;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionActivatedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionCancelledMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.UserRegisteredMessage;
import java.util.Optional;
import java.util.UUID;

public interface ProfileCommandService {
    UserProfile createFromRegistration(UserRegisteredMessage message);

    void applySubscriptionActivated(SubscriptionActivatedMessage message);

    void applySubscriptionCancelled(SubscriptionCancelledMessage message);

    Optional<UserProfile> findByUserId(UUID userId);

    UserProfile findOrCreateForUser(UUID userId);

    UserProfile update(UpdateProfileCommand command);
}
