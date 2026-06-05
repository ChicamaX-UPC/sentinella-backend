package com.chicamax.sentinella.alerts.domain.model.entities;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_rules", schema = "alerts")
public class AlertRule {

    @Id
    private UUID id;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertRuleOperator operator;

    @Column(name = "threshold_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String channels;

    @Column(name = "escalation_minutes")
    private Integer escalationMinutes;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected AlertRule() {
    }

    public AlertRule(
            UUID id,
            UUID nodeId,
            String sensorType,
            AlertRuleOperator operator,
            BigDecimal thresholdValue,
            AlertSeverity severity,
            AlertChannel[] channels,
            Integer escalationMinutes,
            UUID updatedBy
    ) {
        this.id = id;
        this.nodeId = nodeId;
        this.sensorType = sensorType;
        this.operator = operator;
        this.thresholdValue = thresholdValue;
        this.severity = severity;
        this.channels = encodeChannels(channels);
        this.escalationMinutes = escalationMinutes;
        this.active = true;
        this.updatedBy = updatedBy;
        this.updatedAt = OffsetDateTime.now();
    }

    public void update(
            String sensorType,
            AlertRuleOperator operator,
            BigDecimal thresholdValue,
            AlertSeverity severity,
            AlertChannel[] channels,
            Integer escalationMinutes,
            boolean active,
            UUID updatedBy
    ) {
        this.sensorType = sensorType;
        this.operator = operator;
        this.thresholdValue = thresholdValue;
        this.severity = severity;
        this.channels = encodeChannels(channels);
        this.escalationMinutes = escalationMinutes;
        this.active = active;
        this.updatedBy = updatedBy;
        this.updatedAt = OffsetDateTime.now();
    }

    private String encodeChannels(AlertChannel[] channels) {
        if (channels == null || channels.length == 0) {
            return AlertChannel.APP.name();
        }
        return String.join(",", java.util.Arrays.stream(channels).map(Enum::name).toList());
    }

    public AlertChannel[] decodeChannels() {
        return java.util.Arrays.stream(channels.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(AlertChannel::valueOf)
                .toArray(AlertChannel[]::new);
    }

    public UUID getId() {
        return id;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public AlertRuleOperator getOperator() {
        return operator;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public Integer getEscalationMinutes() {
        return escalationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
