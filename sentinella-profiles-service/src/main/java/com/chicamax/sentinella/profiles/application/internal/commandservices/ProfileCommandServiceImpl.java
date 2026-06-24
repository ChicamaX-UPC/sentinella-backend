package com.chicamax.sentinella.profiles.application.internal.commandservices;

import com.chicamax.sentinella.profiles.domain.model.aggregates.UserProfile;
import com.chicamax.sentinella.profiles.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.profiles.domain.model.valueobjects.ActivePlanReference;
import com.chicamax.sentinella.profiles.domain.services.ProfileCommandService;
import com.chicamax.sentinella.profiles.infrastructure.persistence.jpa.UserProfileRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionActivatedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionCancelledMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.UserRegisteredMessage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {

    private final UserProfileRepository userProfileRepository;

    public ProfileCommandServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public UserProfile createFromRegistration(UserRegisteredMessage message) {
        if (userProfileRepository.existsByUserId(message.userId())) {
            return userProfileRepository.findById(message.userId()).orElseThrow();
        }
        return userProfileRepository.save(UserProfile.create(
                message.userId(),
                message.organizationId(),
                message.email(),
                message.fullName(),
                message.companyName()
        ));
    }

    @Override
    @Transactional
    public void applySubscriptionActivated(SubscriptionActivatedMessage message) {
        UserProfile profile = userProfileRepository.findById(message.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        profile.applyActivePlan(new ActivePlanReference(
                message.planType(),
                message.sensorLimit(),
                message.subscriptionId()
        ));
        userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void applySubscriptionCancelled(SubscriptionCancelledMessage message) {
        userProfileRepository.findById(message.userId()).ifPresent(profile -> {
            profile.clearActivePlan();
            userProfileRepository.save(profile);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUserId(UUID userId) {
        return userProfileRepository.findById(userId);
    }

    @Override
    @Transactional
    public UserProfile findOrCreateForUser(UUID userId) {
        return userProfileRepository.findById(userId).orElseGet(() ->
                userProfileRepository.save(UserProfile.create(
                        userId,
                        null,
                        userId + "@usuarios.sentinella",
                        "Usuario",
                        null
                ))
        );
    }

    @Override
    @Transactional
    public UserProfile update(UpdateProfileCommand command) {
        UserProfile profile = userProfileRepository.findById(command.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil no encontrado"));
        profile.updateDetails(command.fullName(), command.phone(), command.jobTitle(), command.preferencesJson());
        return userProfileRepository.save(profile);
    }
}
