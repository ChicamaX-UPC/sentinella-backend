package com.chicamax.sentinella.monitoring.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reading_snapshots", schema = "monitoring")
public class ReadingSnapshot {

    @Id
    private UUID id;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Column(name = "bucket_start", nullable = false)
    private OffsetDateTime bucketStart;

    @Column(name = "avg_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal avgValue;

    @Column(name = "min_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "sample_count", nullable = false)
    private int sampleCount;

    protected ReadingSnapshot() {
    }

    public ReadingSnapshot(
            UUID id,
            UUID nodeId,
            String sensorType,
            OffsetDateTime bucketStart,
            BigDecimal avgValue,
            BigDecimal minValue,
            BigDecimal maxValue,
            int sampleCount
    ) {
        this.id = id;
        this.nodeId = nodeId;
        this.sensorType = sensorType;
        this.bucketStart = bucketStart;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.sampleCount = sampleCount;
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

    public OffsetDateTime getBucketStart() {
        return bucketStart;
    }

    public BigDecimal getAvgValue() {
        return avgValue;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public int getSampleCount() {
        return sampleCount;
    }
}
