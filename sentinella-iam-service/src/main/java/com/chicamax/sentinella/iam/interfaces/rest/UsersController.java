package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.queries.GetAllUsersQuery;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByIdQuery;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.services.UserCommandService;
import com.chicamax.sentinella.iam.domain.services.UserQueryService;
import com.chicamax.sentinella.iam.interfaces.rest.resources.CreateUserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateProfileResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateUserDetailsResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateUserPermissionsResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UpdateUserRoleResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UserResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.UserSummaryResource;
import com.chicamax.sentinella.iam.interfaces.rest.transform.UserResourceAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/users")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserResourceAssembler userResourceAssembler;
    private final AuthorizationScopeService authorizationScopeService;

    public UsersController(
            UserCommandService userCommandService,
            UserQueryService userQueryService,
            UserResourceAssembler userResourceAssembler,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.userResourceAssembler = userResourceAssembler;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping("/assignable")
    public ResponseEntity<List<UserSummaryResource>> getAssignable(@AuthenticationPrincipal Jwt jwt) {
        GetAllUsersQuery query = usersQueryFor(jwt);
        List<UserSummaryResource> users = userQueryService.handle(query)
                .stream()
                .filter(User::isActive)
                .map(u -> new UserSummaryResource(u.getId(), u.getFullName(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResource> getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var user = userQueryService.handle(new GetUserByIdQuery(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return ResponseEntity.ok(userResourceAssembler.toResource(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResource> updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileResource resource
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var updated = userCommandService.updateProfile(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @GetMapping
    public ResponseEntity<List<UserResource>> getUsers(@AuthenticationPrincipal Jwt jwt) {
        requireUserManagement(jwt);
        List<UserResource> resources = userQueryService.handle(usersQueryFor(jwt))
                .stream()
                .map(userResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<UserResource> createUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateUserResource resource
    ) {
        requireUserManagement(jwt);
        if (resource.role() == Role.SYSTEM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido");
        }
        UUID organizationId = authorizationScopeService.requireOrganizationId(jwt);
        var created = userCommandService.createUser(userResourceAssembler.toCommand(resource, organizationId));
        return ResponseEntity.ok(userResourceAssembler.toResource(created));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResource> updateRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleResource resource
    ) {
        requireUserManagement(jwt);
        ensureSameOrganization(jwt, userId);
        var updated = userCommandService.updateRole(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @PatchMapping("/{userId}/permissions")
    public ResponseEntity<UserResource> updatePermissions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserPermissionsResource resource
    ) {
        requireUserManagement(jwt);
        ensureSameOrganization(jwt, userId);
        var updated = userCommandService.updatePermissions(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @PatchMapping("/{userId}/details")
    public ResponseEntity<UserResource> updateDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserDetailsResource resource
    ) {
        requireUserManagement(jwt);
        ensureSameOrganization(jwt, userId);
        var updated = userCommandService.updateDetails(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId
    ) {
        requireUserManagement(jwt);
        ensureSameOrganization(jwt, userId);
        userCommandService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    private void requireUserManagement(Jwt jwt) {
        if (!authorizationScopeService.canManageUsers(jwt)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permiso para gestionar usuarios");
        }
    }

    private void ensureSameOrganization(Jwt jwt, UUID targetUserId) {
        User target = userQueryService.handle(new GetUserByIdQuery(targetUserId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        authorizationScopeService.ensureOrganizationAccess(jwt, target.getOrganizationId());
    }

    private GetAllUsersQuery usersQueryFor(Jwt jwt) {
        return GetAllUsersQuery.forOrganization(authorizationScopeService.requireOrganizationId(jwt));
    }
}
