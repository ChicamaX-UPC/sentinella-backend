package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.domain.model.queries.GetAllUsersQuery;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByIdQuery;
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
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserResourceAssembler userResourceAssembler;

    public UsersController(
            UserCommandService userCommandService,
            UserQueryService userQueryService,
            UserResourceAssembler userResourceAssembler
    ) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.userResourceAssembler = userResourceAssembler;
    }

    @GetMapping("/assignable")
    public ResponseEntity<List<UserSummaryResource>> getAssignable() {
        List<UserSummaryResource> users = userQueryService.handle(new GetAllUsersQuery())
                .stream()
                .filter(u -> u.isActive())
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
    public ResponseEntity<List<UserResource>> getUsers() {
        List<UserResource> resources = userQueryService.handle(new GetAllUsersQuery())
                .stream()
                .map(userResourceAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<UserResource> createUser(@Valid @RequestBody CreateUserResource resource) {
        var created = userCommandService.createUser(userResourceAssembler.toCommand(resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(created));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResource> updateRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleResource resource
    ) {
        var updated = userCommandService.updateRole(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @PatchMapping("/{userId}/permissions")
    public ResponseEntity<UserResource> updatePermissions(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserPermissionsResource resource
    ) {
        var updated = userCommandService.updatePermissions(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @PatchMapping("/{userId}/details")
    public ResponseEntity<UserResource> updateDetails(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserDetailsResource resource
    ) {
        var updated = userCommandService.updateDetails(userResourceAssembler.toCommand(userId, resource));
        return ResponseEntity.ok(userResourceAssembler.toResource(updated));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userCommandService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
