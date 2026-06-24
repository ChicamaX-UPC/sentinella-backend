package com.chicamax.sentinella.blockchain.interfaces.rest;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import com.chicamax.sentinella.blockchain.infrastructure.integration.MonitoringNodeScopeClient;
import com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa.LedgerRecordRepository;
import com.chicamax.sentinella.blockchain.interfaces.rest.resources.LedgerRecordResource;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/blockchain/ledger")
public class LedgerController {

    private final LedgerRecordRepository ledgerRecordRepository;
    private final AuthorizationScopeService authorizationScopeService;
    private final MonitoringNodeScopeClient monitoringNodeScopeClient;

    public LedgerController(
            LedgerRecordRepository ledgerRecordRepository,
            AuthorizationScopeService authorizationScopeService,
            MonitoringNodeScopeClient monitoringNodeScopeClient
    ) {
        this.ledgerRecordRepository = ledgerRecordRepository;
        this.authorizationScopeService = authorizationScopeService;
        this.monitoringNodeScopeClient = monitoringNodeScopeClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<List<LedgerRecordResource>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String entityType
    ) {
        List<UUID> allowedNodeIds = resolveAllowedNodeIds(jwt);
        List<LedgerRecord> rows;
        if (entityId != null) {
            requireNodeAccess(entityId, allowedNodeIds);
            rows = ledgerRecordRepository.findByEntityIdOrderByRegisteredAtDesc(entityId);
        } else if (entityType != null && !entityType.isBlank()) {
            rows = ledgerRecordRepository.findByEntityTypeOrderByRegisteredAtDesc(entityType.trim()).stream()
                    .filter(record -> allowedNodeIds.contains(record.getEntityId()))
                    .toList();
        } else if (allowedNodeIds.isEmpty()) {
            rows = List.of();
        } else {
            rows = ledgerRecordRepository.findByEntityIdInOrderByRegisteredAtDesc(allowedNodeIds);
        }
        return ResponseEntity.ok(rows.stream().map(LedgerController::toResource).toList());
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<LedgerRecordResource> get(
            @PathVariable UUID recordId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        LedgerRecord record = ledgerRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
        requireNodeAccess(record.getEntityId(), resolveAllowedNodeIds(jwt));
        return ResponseEntity.ok(toResource(record));
    }

    private List<UUID> resolveAllowedNodeIds(Jwt jwt) {
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        if (damIds.isEmpty()) {
            return List.of();
        }
        return monitoringNodeScopeClient.resolveNodeIds(damIds);
    }

    private static void requireNodeAccess(UUID entityId, List<UUID> allowedNodeIds) {
        if (allowedNodeIds.isEmpty() || !allowedNodeIds.contains(entityId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al registro solicitado");
        }
    }

    private static LedgerRecordResource toResource(LedgerRecord record) {
        return new LedgerRecordResource(
                record.getId(),
                record.getEntityType(),
                record.getEntityId(),
                record.getContentHash(),
                record.getFabricTxId(),
                record.getRegisteredAt()
        );
    }
}
