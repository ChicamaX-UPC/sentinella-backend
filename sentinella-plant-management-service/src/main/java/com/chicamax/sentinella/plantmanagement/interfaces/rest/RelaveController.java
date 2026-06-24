package com.chicamax.sentinella.plantmanagement.interfaces.rest;

import com.chicamax.sentinella.plantmanagement.domain.services.RelaveCommandService;
import com.chicamax.sentinella.plantmanagement.domain.services.RelaveQueryService;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.CreateRelaveResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RelaveResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.transform.RelaveAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/relaves")
public class RelaveController {

    private final RelaveCommandService relaveCommandService;
    private final RelaveQueryService relaveQueryService;
    private final RelaveAssembler relaveAssembler;
    private final AuthorizationScopeService authorizationScopeService;

    public RelaveController(
            RelaveCommandService relaveCommandService,
            RelaveQueryService relaveQueryService,
            RelaveAssembler relaveAssembler,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.relaveCommandService = relaveCommandService;
        this.relaveQueryService = relaveQueryService;
        this.relaveAssembler = relaveAssembler;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<List<RelaveResource>> listRelaves(@AuthenticationPrincipal Jwt jwt) {
        var content = relaveQueryService.findAll().stream()
                .filter(relave -> authorizationScopeService.canAccessOrganization(jwt, relave.getOrganizationId()))
                .filter(relave -> authorizationScopeService.canAccessDam(jwt, relave.getTailingDamId()))
                .map(relaveAssembler::toResource)
                .toList();
        return ResponseEntity.ok(content);
    }

    @GetMapping("/{relaveId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<RelaveResource> getRelave(
            @PathVariable UUID relaveId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var relave = relaveQueryService.findById(relaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relave no encontrado"));
        if (!authorizationScopeService.canAccessOrganization(jwt, relave.getOrganizationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a la organización");
        }
        if (!authorizationScopeService.canAccessDam(jwt, relave.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque");
        }
        return ResponseEntity.ok(relaveAssembler.toResource(relave));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<RelaveResource> createRelave(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRelaveResource resource
    ) {
        UUID organizationId = authorizationScopeService.requireOrganizationId(jwt);
        UUID userId = UUID.fromString(jwt.getSubject());
        var created = relaveCommandService.create(
                relaveAssembler.toCommand(resource, organizationId, userId)
        );
        return ResponseEntity.ok(relaveAssembler.toResource(created));
    }
}
