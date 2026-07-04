package com.chicamax.sentinella.blockchain.interfaces.rest;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import com.chicamax.sentinella.blockchain.domain.services.LedgerPort;
import com.chicamax.sentinella.blockchain.infrastructure.integration.MonitoringNodeScopeClient;
import com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa.LedgerRecordRepository;
import com.chicamax.sentinella.blockchain.interfaces.rest.resources.LedgerRecordResource;
import com.chicamax.sentinella.blockchain.interfaces.rest.resources.LedgerVerifyResource;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.HashSet;
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
    private final LedgerPort ledgerPort;

    public LedgerController(
            LedgerRecordRepository ledgerRecordRepository,
            AuthorizationScopeService authorizationScopeService,
            MonitoringNodeScopeClient monitoringNodeScopeClient,
            LedgerPort ledgerPort
    ) {
        this.ledgerRecordRepository = ledgerRecordRepository;
        this.authorizationScopeService = authorizationScopeService;
        this.monitoringNodeScopeClient = monitoringNodeScopeClient;
        this.ledgerPort = ledgerPort;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<List<LedgerRecordResource>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String entityType
    ) {
        ScopeContext scope = resolveScope(jwt);
        List<LedgerRecord> rows;
        if (entityId != null) {
            rows = ledgerRecordRepository
                    .findByEntityIdOrRelatedEntityIdOrderByRegisteredAtDesc(entityId)
                    .stream()
                    .filter(record -> isRecordAccessible(record, scope, true))
                    .toList();
        } else if (entityType != null && !entityType.isBlank()) {
            rows = ledgerRecordRepository.findByEntityTypeOrderByRegisteredAtDesc(entityType.trim()).stream()
                    .filter(record -> isRecordAccessible(record, scope, false))
                    .toList();
        } else if (scope.allowedNodeIds().isEmpty() && scope.allowedDamIds().isEmpty()) {
            rows = List.of();
        } else {
            Set<UUID> scopeIds = new HashSet<>(scope.allowedNodeIds());
            scopeIds.addAll(scope.allowedDamIds());
            rows = ledgerRecordRepository.findByNodeIdInOrderByRegisteredAtDesc(scopeIds);
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
        if (!isRecordAccessible(record, resolveScope(jwt), true)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al registro solicitado");
        }
        return ResponseEntity.ok(toResource(record));
    }

    @GetMapping("/verify/{recordId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<LedgerVerifyResource> verify(
            @PathVariable UUID recordId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        LedgerRecord record = ledgerRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
        if (!isRecordAccessible(record, resolveScope(jwt), true)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al registro solicitado");
        }
        boolean onChain = record.isOnChain();
        boolean verified = onChain && ledgerPort.verifyOnChain(
                record.getEntityType(),
                record.getEntityId(),
                record.getContentHash()
        );
        return ResponseEntity.ok(new LedgerVerifyResource(
                record.getId(),
                record.getEntityType(),
                record.getEntityId(),
                record.getContentHash(),
                record.getFabricTxId(),
                true,
                onChain,
                verified
        ));
    }

    private ScopeContext resolveScope(Jwt jwt) {
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        List<UUID> nodeIds = damIds.isEmpty() ? List.of() : monitoringNodeScopeClient.resolveNodeIds(damIds);
        return new ScopeContext(new HashSet<>(nodeIds), damIds);
    }

    private static boolean isRecordAccessible(LedgerRecord record, ScopeContext scope, boolean queriedByEntityId) {
        if (record.getNodeId() == null) {
            return queriedByEntityId;
        }
        if (scope.allowedNodeIds().contains(record.getNodeId())) {
            return true;
        }
        return isDamScopedEntityType(record.getEntityType()) && scope.allowedDamIds().contains(record.getNodeId());
    }

    private static boolean isDamScopedEntityType(String entityType) {
        return "ROUND_SYNC".equals(entityType) || "REGULATORY_REPORT".equals(entityType);
    }

    private static LedgerRecordResource toResource(LedgerRecord record) {
        return new LedgerRecordResource(
                record.getId(),
                record.getEntityType(),
                record.getEntityId(),
                record.getRelatedEntityId(),
                record.getNodeId(),
                record.getContentHash(),
                record.getFabricTxId(),
                record.isOnChain(),
                record.getRegisteredAt()
        );
    }

    private record ScopeContext(Set<UUID> allowedNodeIds, Set<UUID> allowedDamIds) {
    }
}
