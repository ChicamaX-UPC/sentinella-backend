package com.chicamax.sentinella.plantmanagement.domain.model.aggregates;

import com.chicamax.sentinella.plantmanagement.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "iot_nodes", schema = "plant_management")
public class Sensor extends AuditableAbstractAggregateRoot<Sensor> {

    @Id
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "tailing_dam_id", nullable = false)
    private UUID tailingDamId;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "position_3d", columnDefinition = "jsonb")
    private String position3d;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorStatus status;

    protected Sensor() {
    }

    public Sensor(
            UUID id,
            String externalId,
            String name,
            UUID tailingDamId,
            String sensorType,
            BigDecimal latitude,
            BigDecimal longitude,
            String position3d
    ) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.tailingDamId = tailingDamId;
        this.sensorType = sensorType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.position3d = position3d;
        this.status = SensorStatus.ACTIVE;
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

    public String getSensorType() {
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

    public SensorStatus getStatus() {
        return status;
    }
}
