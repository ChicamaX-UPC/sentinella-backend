package com.chicamax.sentinella.alerts.domain.model.aggregates;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "alerts", schema = "alerts")
public class Alert extends AuditableAbstractAggregateRoot<Alert> {

    @Id
    private UUID id;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Column(name = "triggered_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal triggeredValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    protected Alert() {
    }

    public Alert(UUID id, UUID ruleId, UUID nodeId, String sensorType, BigDecimal triggeredValue, AlertSeverity severity) {
        this.id = id;
        this.ruleId = ruleId;
        this.nodeId = nodeId;
        this.sensorType = sensorType;
        this.triggeredValue = triggeredValue;
        this.severity = severity;
        this.status = AlertStatus.ACTIVE;
    }

    public void acknowledge(UUID actorId, OffsetDateTime when) {
        if (status != AlertStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La alerta solo puede reconocerse desde ACTIVE");
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedBy = actorId;
        this.acknowledgedAt = when;
    }

    public void assign(UUID assigneeId) {
        if (status != AlertStatus.ACKNOWLEDGED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La alerta solo puede asignarse desde ACKNOWLEDGED");
        }
        this.status = AlertStatus.IN_PROGRESS;
        this.assignedTo = assigneeId;
    }

    public void close(UUID actorId, OffsetDateTime when, String notes) {
        if (status != AlertStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La alerta solo puede cerrarse desde IN_PROGRESS");
        }
        this.status = AlertStatus.CLOSED;
        this.closedBy = actorId;
        this.closedAt = when;
        this.resolutionNotes = notes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public BigDecimal getTriggeredValue() {
        return triggeredValue;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public UUID getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public OffsetDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public UUID getClosedBy() {
        return closedBy;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }
}
