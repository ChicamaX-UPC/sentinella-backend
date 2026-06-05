package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.domain.model.commands.ForgotPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.commands.RefreshTokenCommand;
import com.chicamax.sentinella.iam.domain.model.commands.ResetPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByEmailQuery;
import com.chicamax.sentinella.iam.domain.services.UserCommandService;
import com.chicamax.sentinella.iam.domain.services.UserQueryService;
import com.chicamax.sentinella.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.ForgotPasswordResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.MessageResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.RefreshTokenResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.ResetPasswordResource;
import com.chicamax.sentinella.iam.domain.model.commands.SignUpCommand;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignInResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignUpResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.TokenResource;
import com.chicamax.sentinella.iam.interfaces.rest.transform.UserResourceAssembler;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/auth")
@SecurityRequirements
public class AuthController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserResourceAssembler userResourceAssembler;

    public AuthController(
            UserCommandService userCommandService,
            UserQueryService userQueryService,
            UserResourceAssembler userResourceAssembler
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.userResourceAssembler = userResourceAssembler;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResource> login(@Valid @RequestBody SignInResource resource) {
        var command = userResourceAssembler.toCommand(resource);
        var tokens = userCommandService.signIn(command);
        var user = userQueryService.handle(new GetUserByEmailQuery(resource.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        return ResponseEntity.ok(userResourceAssembler.toAuthenticatedResource(tokens, user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticatedUserResource> register(@Valid @RequestBody SignUpResource resource) {
        SignUpCommand command = userResourceAssembler.toCommand(resource);
        var tokens = userCommandService.register(command);
        var user = userQueryService.handle(new GetUserByEmailQuery(resource.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario no encontrado"));
        return ResponseEntity.ok(userResourceAssembler.toAuthenticatedResource(tokens, user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        userCommandService.logout(authorization);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResource> refresh(@Valid @RequestBody RefreshTokenResource resource) {
        var tokens = userCommandService.refresh(new RefreshTokenCommand(resource.refreshToken()));
        return ResponseEntity.ok(new TokenResource(tokens.token(), tokens.refreshToken(), tokens.expiresInSeconds()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResource> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        userCommandService.forgotPassword(new ForgotPasswordCommand(resource.email()));
        return ResponseEntity.ok(new MessageResource("Si el correo existe, se envio un enlace de recuperacion"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResource> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        userCommandService.resetPassword(new ResetPasswordCommand(resource.token(), resource.newPassword()));
        return ResponseEntity.ok(new MessageResource("Contrasena actualizada"));
    }
}
