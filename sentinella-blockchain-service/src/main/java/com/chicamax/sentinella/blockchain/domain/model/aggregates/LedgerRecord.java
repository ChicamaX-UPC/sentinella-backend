package com.chicamax.sentinella.blockchain.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_records", schema = "blockchain")
public class LedgerRecord {

    @Id
    private UUID id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "fabric_tx_id")
    private String fabricTxId;

    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt;

    protected LedgerRecord() {
    }

    public static LedgerRecord register(UUID id, String entityType, UUID entityId, String contentHash) {
        LedgerRecord record = new LedgerRecord();
        record.id = id;
        record.entityType = entityType;
        record.entityId = entityId;
        record.contentHash = contentHash;
        record.fabricTxId = "stub-" + id.toString().substring(0, 8);
        record.registeredAt = OffsetDateTime.now();
        return record;
    }
}
