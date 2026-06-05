package com.chicamax.sentinella.monitoring.domain.model.entities;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdChannel;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdSeverity;
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
@Table(name = "threshold_rules", schema = "monitoring")
public class ThresholdRule {

    @Id
    private UUID id;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThresholdRuleOperator operator;

    @Column(name = "threshold_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThresholdSeverity severity;

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

    protected ThresholdRule() {
    }

    public ThresholdRule(
            UUID id,
            UUID nodeId,
            String sensorType,
            ThresholdRuleOperator operator,
            BigDecimal thresholdValue,
            ThresholdSeverity severity,
            ThresholdChannel[] channels,
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

    public static ThresholdRule create(
            UUID id,
            UUID nodeId,
            String sensorType,
            ThresholdRuleOperator operator,
            BigDecimal thresholdValue,
            String severity
    ) {
        ThresholdSeverity sev;
        try {
            sev = ThresholdSeverity.valueOf(severity);
        } catch (IllegalArgumentException ex) {
            sev = ThresholdSeverity.WARNING;
        }
        return new ThresholdRule(id, nodeId, sensorType, operator, thresholdValue, sev, new ThresholdChannel[] {ThresholdChannel.APP}, null, null);
    }

    public void update(
            String sensorType,
            ThresholdRuleOperator operator,
            BigDecimal thresholdValue,
            ThresholdSeverity severity,
            ThresholdChannel[] channels,
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

    private String encodeChannels(ThresholdChannel[] channels) {
        if (channels == null || channels.length == 0) {
            return ThresholdChannel.APP.name();
        }
        return String.join(",", java.util.Arrays.stream(channels).map(Enum::name).toList());
    }

    public ThresholdChannel[] decodeChannels() {
        return java.util.Arrays.stream(channels.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(ThresholdChannel::valueOf)
                .toArray(ThresholdChannel[]::new);
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

    public ThresholdRuleOperator getOperator() {
        return operator;
    }

    public BigDecimal getThresholdValue() {
        return thresholdValue;
    }

    public ThresholdSeverity getSeverity() {
        return severity;
    }

    public String getChannels() {
        return channels;
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
