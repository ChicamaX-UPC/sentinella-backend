package com.chicamax.sentinella.simulations.interfaces.rest;

import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioCommandService;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioQueryService;
import com.chicamax.sentinella.simulations.interfaces.rest.resources.CreateSimulationScenarioResource;
import com.chicamax.sentinella.simulations.interfaces.rest.resources.SimulationScenarioResource;
import com.chicamax.sentinella.simulations.interfaces.rest.resources.UpdateSimulationScenarioResource;
import com.chicamax.sentinella.simulations.interfaces.rest.transform.SimulationScenarioAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/simulation-scenarios")
public class SimulationScenariosController {

    private final SimulationScenarioCommandService commandService;
    private final SimulationScenarioQueryService queryService;
    private final SimulationScenarioAssembler assembler;
    private final AuthorizationScopeService authorizationScopeService;

    public SimulationScenariosController(
            SimulationScenarioCommandService commandService,
            SimulationScenarioQueryService queryService,
            SimulationScenarioAssembler assembler,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.assembler = assembler;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<List<SimulationScenarioResource>> list(@AuthenticationPrincipal Jwt jwt) {
        List<SimulationScenarioResource> list = queryService.listForJwt(jwt).stream()
                .map(assembler::toResource)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<SimulationScenarioResource> create(
            @Valid @RequestBody CreateSimulationScenarioResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (!authorizationScopeService.canAccessDam(jwt, resource.tailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID createdBy = UUID.fromString(jwt.getSubject());
        var saved = commandService.create(assembler.toCreateCommand(resource, createdBy));
        return ResponseEntity.ok(assembler.toResource(saved));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<SimulationScenarioResource> getById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var scenario = queryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        if (!queryService.canRead(jwt, scenario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a este escenario");
        }
        return ResponseEntity.ok(assembler.toResource(scenario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<SimulationScenarioResource> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateSimulationScenarioResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var existing = queryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        if (!authorizationScopeService.canAccessDam(jwt, existing.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID actorId = UUID.fromString(jwt.getSubject());
        String role = jwt.getClaimAsString("role");
        var updated = commandService.update(assembler.toUpdateCommand(id, resource, actorId, role));
        return ResponseEntity.ok(assembler.toResource(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        var existing = queryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        if (!authorizationScopeService.canAccessDam(jwt, existing.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID actorId = UUID.fromString(jwt.getSubject());
        String role = jwt.getClaimAsString("role");
        commandService.delete(id, actorId, role);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<SimulationScenarioResource> publish(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        var existing = queryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        if (!authorizationScopeService.canAccessDam(jwt, existing.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID actorId = UUID.fromString(jwt.getSubject());
        String role = jwt.getClaimAsString("role");
        var updated = commandService.publish(id, actorId, role);
        return ResponseEntity.ok(assembler.toResource(updated));
    }

    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<SimulationScenarioResource> unpublish(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        var existing = queryService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        if (!authorizationScopeService.canAccessDam(jwt, existing.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID actorId = UUID.fromString(jwt.getSubject());
        String role = jwt.getClaimAsString("role");
        var updated = commandService.unpublish(id, actorId, role);
        return ResponseEntity.ok(assembler.toResource(updated));
    }
}
