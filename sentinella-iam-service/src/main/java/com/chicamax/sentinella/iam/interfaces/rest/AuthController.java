package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.domain.model.commands.ForgotPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.commands.RefreshTokenCommand;
import com.chicamax.sentinella.iam.domain.model.commands.ResetPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByEmailQuery;
import com.chicamax.sentinella.iam.domain.services.UserCommandService;
import com.chicamax.sentinella.iam.domain.services.UserQueryService;
import com.chicamax.sentinella.iam.infrastructure.security.AuthRateLimiter;
import com.chicamax.sentinella.iam.infrastructure.security.WebSocketTicketService;
import com.chicamax.sentinella.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.ForgotPasswordResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.MessageResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.RefreshTokenResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.ResetPasswordResource;
import com.chicamax.sentinella.iam.domain.model.commands.SignUpCommand;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignInResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.SignUpResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.TokenResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.WsTicketResource;
import com.chicamax.sentinella.iam.interfaces.rest.transform.UserResourceAssembler;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final AuthRateLimiter authRateLimiter;
    private final WebSocketTicketService webSocketTicketService;
    private final boolean openRegistration;

    public AuthController(
            UserCommandService userCommandService,
            UserQueryService userQueryService,
            UserResourceAssembler userResourceAssembler,
            AuthRateLimiter authRateLimiter,
            WebSocketTicketService webSocketTicketService,
            @Value("${sentinella.auth.open-registration-enabled:true}") boolean openRegistration
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.userResourceAssembler = userResourceAssembler;
        this.authRateLimiter = authRateLimiter;
        this.webSocketTicketService = webSocketTicketService;
        this.openRegistration = openRegistration;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResource> login(
            @Valid @RequestBody SignInResource resource,
            HttpServletRequest request
    ) {
        authRateLimiter.check(request);
        var command = userResourceAssembler.toCommand(resource);
        var tokens = userCommandService.signIn(command);
        var user = userQueryService.handle(new GetUserByEmailQuery(resource.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        return ResponseEntity.ok(userResourceAssembler.toAuthenticatedResource(tokens, user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticatedUserResource> register(@Valid @RequestBody SignUpResource resource) {
        if (!openRegistration) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registro publico deshabilitado");
        }
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
    public ResponseEntity<MessageResource> forgotPassword(
            @Valid @RequestBody ForgotPasswordResource resource,
            HttpServletRequest request
    ) {
        authRateLimiter.check(request);
        userCommandService.forgotPassword(new ForgotPasswordCommand(resource.email()));
        return ResponseEntity.ok(new MessageResource("Si el correo existe, se envio un enlace de recuperacion"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResource> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        userCommandService.resetPassword(new ResetPasswordCommand(resource.token(), resource.newPassword()));
        return ResponseEntity.ok(new MessageResource("Contrasena actualizada"));
    }

    @PostMapping("/ws-ticket")
    public ResponseEntity<WsTicketResource> issueWsTicket(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        String role = jwt.getClaimAsString("role");
        String ticket = webSocketTicketService.issue(UUID.fromString(jwt.getSubject()), role);
        return ResponseEntity.ok(new WsTicketResource(ticket));
    }
}
