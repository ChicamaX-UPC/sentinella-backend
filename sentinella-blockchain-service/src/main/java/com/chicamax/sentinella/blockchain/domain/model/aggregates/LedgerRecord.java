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

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "node_id")
    private UUID nodeId;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Column(name = "fabric_tx_id")
    private String fabricTxId;

    @Column(name = "on_chain", nullable = false)
    private boolean onChain;

    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt;

    protected LedgerRecord() {
    }

    public static LedgerRecord register(
            UUID id,
            String entityType,
            UUID entityId,
            UUID nodeId,
            UUID relatedEntityId,
            String contentHash,
            String fabricTxId,
            boolean onChain
    ) {
        LedgerRecord record = new LedgerRecord();
        record.id = id;
        record.entityType = entityType;
        record.entityId = entityId;
        record.nodeId = nodeId;
        record.relatedEntityId = relatedEntityId;
        record.contentHash = contentHash;
        record.fabricTxId = fabricTxId;
        record.onChain = onChain;
        record.registeredAt = OffsetDateTime.now();
        return record;
    }

    public UUID getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public UUID getRelatedEntityId() {
        return relatedEntityId;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getFabricTxId() {
        return fabricTxId;
    }

    public boolean isOnChain() {
        return onChain;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }
}
