package com.chicamax.sentinella.alerts.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_audit_log", schema = "alerts")
public class AlertAuditEntry {

    @Id
    private UUID id;

    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    @Column(nullable = false)
    private String action;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @Column
    private String notes;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    protected AlertAuditEntry() {
    }

    public AlertAuditEntry(UUID id, UUID alertId, String action, UUID actorId, String actorRole, String notes) {
        this.id = id;
        this.alertId = alertId;
        this.action = action;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.notes = notes;
        this.timestamp = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAlertId() {
        return alertId;
    }

    public String getAction() {
        return action;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
