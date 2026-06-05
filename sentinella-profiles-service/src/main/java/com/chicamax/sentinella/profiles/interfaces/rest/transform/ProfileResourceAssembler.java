package com.chicamax.sentinella.profiles.interfaces.rest.transform;

import com.chicamax.sentinella.profiles.domain.model.aggregates.UserProfile;
import com.chicamax.sentinella.profiles.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.profiles.interfaces.rest.resources.PatchProfileResource;
import com.chicamax.sentinella.profiles.interfaces.rest.resources.ProfileResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProfileResourceAssembler {

    public ProfileResource toResource(UserProfile profile) {
        return new ProfileResource(
                profile.getUserId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getJobTitle(),
                profile.getPlanType(),
                profile.getSensorLimit(),
                profile.getSubscriptionId(),
                profile.getPreferences()
        );
    }

    public UpdateProfileCommand toCommand(UUID userId, PatchProfileResource resource) {
        return new UpdateProfileCommand(
                userId,
                resource.fullName(),
                resource.phone(),
                resource.jobTitle(),
                resource.preferences()
        );
    }
}
