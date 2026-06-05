package com.chicamax.sentinella.monitoring.domain.model.aggregates;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sensor_nodes", schema = "monitoring")
public class SensorNode extends AuditableAbstractAggregateRoot<SensorNode> {

    @Id
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "tailing_dam_id", nullable = false)
    private UUID tailingDamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false)
    private SensorType sensorType;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "position_3d", columnDefinition = "jsonb")
    private String position3d;

    @Column(nullable = false)
    private String status;

    @Column(name = "last_seen")
    private OffsetDateTime lastSeen;

    protected SensorNode() {
    }

    /** Construcción explícita para datos iniciales / tests (JPA sigue usando el ctor protegido vacío). */
    public SensorNode(
            UUID id,
            String externalId,
            String name,
            UUID tailingDamId,
            SensorType sensorType,
            BigDecimal latitude,
            BigDecimal longitude,
            String position3d,
            String status,
            OffsetDateTime lastSeen
    ) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.tailingDamId = tailingDamId;
        this.sensorType = sensorType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.position3d = position3d;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public UUID getTailingDamId() {
        return tailingDamId;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getPosition3d() {
        return position3d;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getLastSeen() {
        return lastSeen;
    }

    public void recordContact(OffsetDateTime at) {
        if (at == null) {
            return;
        }
        this.lastSeen = at;
    }
}
