package com.chicamax.sentinella.fieldoperations.interfaces.rest;

import com.chicamax.sentinella.fieldoperations.domain.model.commands.SyncRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.queries.GetRoundsByOperatorQuery;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundCommandService;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundQueryService;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.CompleteChecklistItemResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.CreateRoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.RoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.SyncRoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.transform.InspectionRoundAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/rounds")
public class FieldOperationsController {

    private final InspectionRoundCommandService inspectionRoundCommandService;
    private final InspectionRoundQueryService inspectionRoundQueryService;
    private final InspectionRoundAssembler inspectionRoundAssembler;
    private final AuthorizationScopeService authorizationScopeService;

    public FieldOperationsController(
            InspectionRoundCommandService inspectionRoundCommandService,
            InspectionRoundQueryService inspectionRoundQueryService,
            InspectionRoundAssembler inspectionRoundAssembler,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.inspectionRoundCommandService = inspectionRoundCommandService;
        this.inspectionRoundQueryService = inspectionRoundQueryService;
        this.inspectionRoundAssembler = inspectionRoundAssembler;
        this.authorizationScopeService = authorizationScopeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<RoundResource> createRound(
            @Valid @RequestBody CreateRoundResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (!authorizationScopeService.canAccessDam(jwt, resource.tailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID operatorId = UUID.fromString(jwt.getSubject());
        var created = inspectionRoundCommandService.createRound(inspectionRoundAssembler.toCommand(resource, operatorId));
        var items = inspectionRoundQueryService.findChecklistItemsByRoundId(created.getId());
        return ResponseEntity.ok(inspectionRoundAssembler.toResource(created, items));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<List<RoundResource>> getRounds(@AuthenticationPrincipal Jwt jwt) {
        UUID operatorId = UUID.fromString(jwt.getSubject());
        List<RoundResource> rounds = inspectionRoundQueryService.handle(new GetRoundsByOperatorQuery(operatorId))
                .stream()
                .map(inspectionRoundAssembler::toResource)
                .toList();
        return ResponseEntity.ok(rounds);
    }

    @GetMapping("/{roundId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<RoundResource> getRound(
            @PathVariable UUID roundId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var round = inspectionRoundQueryService.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        if (!authorizationScopeService.canAccessDam(jwt, round.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        var items = inspectionRoundQueryService.findChecklistItemsByRoundId(roundId);
        return ResponseEntity.ok(inspectionRoundAssembler.toResource(round, items));
    }

    @PatchMapping("/{roundId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<RoundResource> completeItem(
            @PathVariable UUID roundId,
            @PathVariable UUID itemId,
            @RequestBody CompleteChecklistItemResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var round = inspectionRoundQueryService.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        if (!authorizationScopeService.canAccessDam(jwt, round.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        var updated = inspectionRoundCommandService.completeChecklistItem(
                inspectionRoundAssembler.toCommand(roundId, itemId, resource)
        );
        var items = inspectionRoundQueryService.findChecklistItemsByRoundId(roundId);
        return ResponseEntity.ok(inspectionRoundAssembler.toResource(updated, items));
    }

    @PostMapping("/{roundId}/sync")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<RoundResource> syncRound(
            @PathVariable UUID roundId,
            @RequestBody(required = false) SyncRoundResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var round = inspectionRoundQueryService.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        if (!authorizationScopeService.canAccessDam(jwt, round.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        var synced = inspectionRoundCommandService.syncRound(new SyncRoundCommand(roundId));
        var itemsAfter = inspectionRoundQueryService.findChecklistItemsByRoundId(roundId);
        return ResponseEntity.ok(inspectionRoundAssembler.toResource(synced, itemsAfter));
    }
}
