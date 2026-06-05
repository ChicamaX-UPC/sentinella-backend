package com.chicamax.sentinella.iam.interfaces.rest.transform;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.commands.CreateUserCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignInCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignUpCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserRoleCommand;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import com.chicamax.sentinella.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.CreateUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignInResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignUpResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateProfileResource;
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
        return new SignUpCommand(resource.email(), resource.password(), resource.fullName());
    }

    public CreateUserCommand toCommand(CreateUserResource resource) {
        return new CreateUserCommand(
                resource.email(),
                resource.password(),
                resource.fullName(),
                resource.role(),
                resource.tailingDamIds()
        );
    }

    public UpdateUserRoleCommand toCommand(UUID userId, UpdateUserRoleResource resource) {
        return new UpdateUserRoleCommand(userId, resource.role());
    }

    public UpdateProfileCommand toCommand(UUID userId, UpdateProfileResource resource) {
        return new UpdateProfileCommand(userId, resource.fullName(), resource.currentPassword(), resource.newPassword());
    }

    public UserResource toResource(User user) {
        return new UserResource(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getTailingDamIds(),
                user.isActive(),
                user.getLastLogin()
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
