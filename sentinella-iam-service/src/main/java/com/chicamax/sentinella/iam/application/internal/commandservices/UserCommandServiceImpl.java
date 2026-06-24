package com.chicamax.sentinella.iam.application.internal.commandservices;

import com.chicamax.sentinella.iam.domain.model.aggregates.Organization;
import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.commands.CreateUserCommand;
import com.chicamax.sentinella.iam.domain.model.commands.ForgotPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.commands.RefreshTokenCommand;
import com.chicamax.sentinella.iam.domain.model.commands.ResetPasswordCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignInCommand;
import com.chicamax.sentinella.iam.domain.model.commands.SignUpCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateProfileCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserDetailsCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserPermissionsCommand;
import com.chicamax.sentinella.iam.domain.model.commands.UpdateUserRoleCommand;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.model.valueobjects.TokenType;
import com.chicamax.sentinella.iam.domain.services.HashingService;
import com.chicamax.sentinella.iam.domain.services.TokenService;
import com.chicamax.sentinella.iam.domain.services.UserCommandService;
import com.chicamax.sentinella.iam.infrastructure.messaging.UserRegisteredRabbitPublisher;
import com.chicamax.sentinella.iam.infrastructure.persistence.jpa.OrganizationRepository;
import com.chicamax.sentinella.iam.infrastructure.persistence.jpa.UserRepository;
import com.chicamax.sentinella.iam.infrastructure.security.TokenRevocationService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.UserRegisteredMessage;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;
    private static final int RESET_TOKEN_MINUTES = 15;

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final UserRegisteredRabbitPublisher userRegisteredRabbitPublisher;
    private final TokenRevocationService tokenRevocationService;
    private final Map<String, ResetTokenData> passwordResetTokens = new ConcurrentHashMap<>();

    public UserCommandServiceImpl(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            HashingService hashingService,
            TokenService tokenService,
            UserRegisteredRabbitPublisher userRegisteredRabbitPublisher,
            TokenRevocationService tokenRevocationService
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.userRegisteredRabbitPublisher = userRegisteredRabbitPublisher;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    @Transactional
    public AuthTokens signIn(SignInCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        OffsetDateTime now = OffsetDateTime.now();
        if (user.isLockedAt(now)) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Cuenta bloqueada temporalmente");
        }

        if (!hashingService.matches(command.password(), user.getPasswordHash())) {
            user.registerFailedAttempt(now, MAX_FAILED_ATTEMPTS, LOCK_MINUTES);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        user.registerSuccessfulLogin(now);
        userRepository.save(user);
        return tokenService.issueTokens(user);
    }

    @Override
    @Transactional
    public AuthTokens register(SignUpCommand command) {
        Organization organization = organizationRepository.save(
                new Organization(UUID.randomUUID(), command.companyName())
        );
        User saved = persistNewUser(
                command.email(),
                command.password(),
                command.fullName(),
                Role.PLANT_MANAGER,
                organization.getId(),
                new UUID[0]
        );
        userRegisteredRabbitPublisher.publish(new UserRegisteredMessage(
                saved.getId(),
                organization.getId(),
                saved.getEmail(),
                saved.getFullName(),
                command.companyName()
        ));
        return tokenService.issueTokens(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokens refresh(RefreshTokenCommand command) {
        var decodedToken = tokenService.decode(command.refreshToken());
        if (decodedToken.type() != TokenType.REFRESH) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
        }

        User user = userRepository.findById(decodedToken.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido"));
        return tokenService.issueTokens(user);
    }

    @Override
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isBlank()) {
            return;
        }
        try {
            var decoded = tokenService.decode(token);
            if (decoded.type() != TokenType.ACCESS) {
                return;
            }
            tokenRevocationService.revoke(token, decoded.expiresAt());
        } catch (Exception ignored) {
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordCommand command) {
        userRepository.findByEmail(command.email()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            passwordResetTokens.put(resetToken, new ResetTokenData(user.getId(), OffsetDateTime.now().plusMinutes(RESET_TOKEN_MINUTES)));
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        ResetTokenData data = passwordResetTokens.remove(command.token());
        if (data == null || data.expiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperacion invalido");
        }
        User user = userRepository.findById(data.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de recuperacion invalido"));
        user.updatePasswordHash(hashingService.hash(command.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        if (command.role() == Role.SYSTEM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede crear administradores de plataforma");
        }
        User saved = persistNewUser(
                command.email(),
                command.password(),
                command.fullName(),
                command.role(),
                command.organizationId(),
                command.tailingDamIds()
        );
        userRegisteredRabbitPublisher.publish(new UserRegisteredMessage(
                saved.getId(),
                saved.getOrganizationId(),
                saved.getEmail(),
                saved.getFullName(),
                null
        ));
        return saved;
    }

    private User persistNewUser(
            String email,
            String password,
            String fullName,
            Role role,
            UUID organizationId,
            UUID[] tailingDamIds
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya existe");
        }

        User user = new User(
                UUID.randomUUID(),
                email,
                hashingService.hash(password),
                fullName,
                role,
                organizationId,
                tailingDamIds
        );
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateRole(UpdateUserRoleCommand command) {
        User user = requireUser(command.userId());
        if (command.role() == Role.SYSTEM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        }
        user.updateRole(command.role());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(UpdateProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        user.updateFullName(command.fullName().trim());

        String newPassword = command.newPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            String current = command.currentPassword();
            if (current == null || current.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique la contrasena actual");
            }
            if (!hashingService.matches(current, user.getPasswordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contrasena actual incorrecta");
            }
            user.updatePasswordHash(hashingService.hash(newPassword));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updatePermissions(UpdateUserPermissionsCommand command) {
        User user = requireUser(command.userId());
        user.updatePermissions(command.permissions());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateDetails(UpdateUserDetailsCommand command) {
        User user = requireUser(command.userId());
        user.updateProfileDetails(
                command.fullName().trim(),
                blankToNull(command.jobTitle()),
                blankToNull(command.phone())
        );
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void assignTailingDam(UUID userId, UUID organizationId, UUID tailingDamId) {
        User user = requireUser(userId);
        if (!user.getOrganizationId().equals(organizationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario fuera de la organización");
        }
        user.addTailingDam(tailingDamId);
        userRepository.save(user);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record ResetTokenData(UUID userId, OffsetDateTime expiresAt) {
    }
}
