package com.chicamax.sentinella.fieldoperations.domain.model.aggregates;

import com.chicamax.sentinella.fieldoperations.domain.model.valueobjects.RoundStatus;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift_reports", schema = "plant_management")
public class InspectionRound extends AuditableAbstractAggregateRoot<InspectionRound> {

    @Id
    private UUID id;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "tailing_dam_id", nullable = false)
    private UUID tailingDamId;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStatus status;

    @Column(name = "offline_created", nullable = false)
    private boolean offlineCreated;

    @Column(name = "synced_at")
    private OffsetDateTime syncedAt;

    protected InspectionRound() {
    }

    public InspectionRound(UUID id, UUID operatorId, UUID tailingDamId, OffsetDateTime scheduledAt, boolean offlineCreated) {
        this.id = id;
        this.operatorId = operatorId;
        this.tailingDamId = tailingDamId;
        this.scheduledAt = scheduledAt;
        this.offlineCreated = offlineCreated;
        this.status = RoundStatus.PENDING;
    }

    public void start() {
        if (status == RoundStatus.PENDING) {
            status = RoundStatus.IN_PROGRESS;
            startedAt = OffsetDateTime.now();
        }
    }

    public void complete() {
        if (status == RoundStatus.IN_PROGRESS) {
            status = RoundStatus.COMPLETED;
            completedAt = OffsetDateTime.now();
        }
    }

    public void markSynced() {
        if (status == RoundStatus.COMPLETED || status == RoundStatus.IN_PROGRESS) {
            status = RoundStatus.SYNCED;
            syncedAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public UUID getTailingDamId() {
        return tailingDamId;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public boolean isOfflineCreated() {
        return offlineCreated;
    }

    public OffsetDateTime getSyncedAt() {
        return syncedAt;
    }
}
