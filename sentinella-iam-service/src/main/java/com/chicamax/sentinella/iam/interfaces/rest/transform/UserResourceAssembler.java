package com.chicamax.sentinella.iam.interfaces.rest.transform;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.commands.CreateUserCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignInCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignUpCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserDetailsCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserPermissionsCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserRoleCommand;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import com.chicamax.sentinella.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.CreateUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignInResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignUpResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateProfileResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateUserDetailsResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateUserPermissionsResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateUserRoleResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UserResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserResourceAssembler {

    public SignInCommand toCommand(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }

    public SignUpCommand toCommand(SignUpResource resource) {
        return new SignUpCommand(resource.email(), resource.password(), resource.fullName(), resource.companyName());
    }

    public CreateUserCommand toCommand(CreateUserResource resource, UUID organizationId) {
        return new CreateUserCommand(
                resource.email(),
                resource.password(),
                resource.fullName(),
                resource.role(),
                organizationId,
                resource.tailingDamIds()
        );
    }

    public UpdateUserRoleCommand toCommand(UUID userId, UpdateUserRoleResource resource) {
        return new UpdateUserRoleCommand(userId, resource.role());
    }

    public UpdateProfileCommand toCommand(UUID userId, UpdateProfileResource resource) {
        return new UpdateProfileCommand(
                userId,
                resource.fullName(),
                resource.jobTitle(),
                resource.phone(),
                resource.currentPassword(),
                resource.newPassword()
        );
    }

    public UpdateUserPermissionsCommand toCommand(UUID userId, UpdateUserPermissionsResource resource) {
        return new UpdateUserPermissionsCommand(
                userId,
                resource.permissions().toArray(String[]::new)
        );
    }

    public UpdateUserDetailsCommand toCommand(UUID userId, UpdateUserDetailsResource resource) {
        return new UpdateUserDetailsCommand(userId, resource.fullName(), resource.jobTitle(), resource.phone());
    }

    public UserResource toResource(User user) {
        return new UserResource(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getOrganizationId(),
                user.getTailingDamIds(),
                user.isActive(),
                user.getLastLogin(),
                user.getEffectivePermissions(),
                user.getJobTitle(),
                user.getPhone()
        );
    }

    public AuthenticatedUserResource toAuthenticatedResource(AuthTokens tokens, User user) {
        return new AuthenticatedUserResource(
                tokens.token(),
                tokens.refreshToken(),
                tokens.expiresInSeconds(),
                toResource(user)
        );
    }
}
