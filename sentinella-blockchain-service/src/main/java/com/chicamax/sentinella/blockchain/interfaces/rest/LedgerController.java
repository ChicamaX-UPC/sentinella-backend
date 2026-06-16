package com.chicamax.sentinella.blockchain.interfaces.rest;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa.LedgerRecordRepository;
import com.chicamax.sentinella.blockchain.interfaces.rest.resources.LedgerRecordResource;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    public LedgerController(LedgerRecordRepository ledgerRecordRepository) {
        this.ledgerRecordRepository = ledgerRecordRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<List<LedgerRecordResource>> list(
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String entityType
    ) {
        List<LedgerRecord> rows;
        if (entityId != null) {
            rows = ledgerRecordRepository.findByEntityIdOrderByRegisteredAtDesc(entityId);
        } else if (entityType != null && !entityType.isBlank()) {
            rows = ledgerRecordRepository.findByEntityTypeOrderByRegisteredAtDesc(entityType.trim());
        } else {
            rows = ledgerRecordRepository.findAll();
        }
        return ResponseEntity.ok(rows.stream().map(LedgerController::toResource).toList());
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<LedgerRecordResource> get(@PathVariable UUID recordId) {
        LedgerRecord record = ledgerRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
        return ResponseEntity.ok(toResource(record));
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
