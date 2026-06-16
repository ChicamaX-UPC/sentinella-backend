package com.chicamax.sentinella.fieldoperations.interfaces.rest;

import com.chicamax.sentinella.fieldoperations.domain.model.commands.SyncRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.queries.GetRoundsByOperatorQuery;
import com.chicamax.sentinella.fieldoperations.domain.services.ChecklistPhotoStorage;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundCommandService;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundQueryService;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.ChecklistItemRepository;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.ChecklistPhotoUploadResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.CompleteChecklistItemResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.CreateRoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.RoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.SyncRoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.transform.InspectionRoundAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import com.chicamax.sentinella.shared.interfaces.rest.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/rounds")
public class FieldOperationsController {

    private final InspectionRoundCommandService inspectionRoundCommandService;
    private final InspectionRoundQueryService inspectionRoundQueryService;
    private final InspectionRoundAssembler inspectionRoundAssembler;
    private final AuthorizationScopeService authorizationScopeService;
    private final ChecklistItemRepository checklistItemRepository;
    private final ChecklistPhotoStorage checklistPhotoStorage;

    public FieldOperationsController(
            InspectionRoundCommandService inspectionRoundCommandService,
            InspectionRoundQueryService inspectionRoundQueryService,
            InspectionRoundAssembler inspectionRoundAssembler,
            AuthorizationScopeService authorizationScopeService,
            ChecklistItemRepository checklistItemRepository,
            ChecklistPhotoStorage checklistPhotoStorage
    ) {
        this.inspectionRoundCommandService = inspectionRoundCommandService;
        this.inspectionRoundQueryService = inspectionRoundQueryService;
        this.inspectionRoundAssembler = inspectionRoundAssembler;
        this.authorizationScopeService = authorizationScopeService;
        this.checklistItemRepository = checklistItemRepository;
        this.checklistPhotoStorage = checklistPhotoStorage;
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
    public ResponseEntity<PageResponse<RoundResource>> getRounds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID operatorId = UUID.fromString(jwt.getSubject());
        var result = inspectionRoundQueryService.handle(new GetRoundsByOperatorQuery(operatorId, page, limit));
        var content = result.getContent().stream().map(inspectionRoundAssembler::toResource).toList();
        return ResponseEntity.ok(PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements()));
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

    @PostMapping(value = "/{roundId}/items/{itemId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<ChecklistPhotoUploadResource> uploadChecklistPhoto(
            @PathVariable UUID roundId,
            @PathVariable UUID itemId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var round = inspectionRoundQueryService.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        if (!authorizationScopeService.canAccessDam(jwt, round.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        checklistItemRepository.findByIdAndRoundId(itemId, roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item de checklist no encontrado"));
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacio");
        }
        try {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo.jpg";
            String key = checklistPhotoStorage.store(
                    roundId,
                    itemId,
                    file.getBytes(),
                    file.getContentType(),
                    filename
            );
            return ResponseEntity.ok(new ChecklistPhotoUploadResource(key));
        } catch (java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo invalido");
        }
    }

    @GetMapping("/{roundId}/items/{itemId}/photo")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<byte[]> downloadChecklistPhoto(
            @PathVariable UUID roundId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var round = inspectionRoundQueryService.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        if (!authorizationScopeService.canAccessDam(jwt, round.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        var item = checklistItemRepository.findByIdAndRoundId(itemId, roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item de checklist no encontrado"));
        String key = item.getPhotoS3Key();
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin foto adjunta");
        }
        var photo = checklistPhotoStorage.load(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .body(photo.content());
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
