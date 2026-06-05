package com.chicamax.sentinella.monitoring.domain.model.entities;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sensor_readings", schema = "monitoring")
@IdClass(SensorReading.SensorReadingId.class)
public class SensorReading {

    @Id
    private UUID id;

    @Id
    private OffsetDateTime timestamp;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal value;

    @Column(nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb")
    private String rawPayload;

    protected SensorReading() {
    }

    public SensorReading(
            UUID id,
            OffsetDateTime timestamp,
            UUID nodeId,
            SensorType sensorType,
            BigDecimal value,
            String unit,
            SensorStatus status,
            String rawPayload
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.nodeId = nodeId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.status = status;
        this.rawPayload = rawPayload;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public SensorStatus getStatus() {
        return status;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public static class SensorReadingId implements Serializable {
        private UUID id;
        private OffsetDateTime timestamp;

        public SensorReadingId() {
        }

        public SensorReadingId(UUID id, OffsetDateTime timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SensorReadingId that = (SensorReadingId) o;
            return Objects.equals(id, that.id) && Objects.equals(timestamp, that.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, timestamp);
        }
    }
}
