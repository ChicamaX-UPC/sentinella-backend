package com.chicamax.sentinella.iam.domain.services;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.commands.CreateUserCommand;
import com.chicamax.sentinella.iam.domain.model.commands.ForgotPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.commands.RefreshTokenCommand;
import com.chicamax.sentinella.iam.domain.model.commands.ResetPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignInCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignUpCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserRoleCommand;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import java.util.UUID;

public interface UserCommandService {
    AuthTokens signIn(SignInCommand command);

    /** Registro público (rol READ_ONLY, sin tranques hasta que un admin asigne). */
    AuthTokens register(SignUpCommand command);

    AuthTokens refresh(RefreshTokenCommand command);

    void logout(String authorizationHeader);

    void forgotPassword(ForgotPasswordCommand command);

    void resetPassword(ResetPasswordCommand command);

    User createUser(CreateUserCommand command);

    User updateRole(UpdateUserRoleCommand command);

    User updateProfile(UpdateProfileCommand command);

    void deleteUser(UUID userId);
}
