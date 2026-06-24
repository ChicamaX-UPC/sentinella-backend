package com.chicamax.sentinella.plantmanagement.domain.model.aggregates;

import com.chicamax.sentinella.plantmanagement.domain.model.valueobjects.RelaveStatus;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "relaves", schema = "plant_management")
public class Relave extends AuditableAbstractAggregateRoot<Relave> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "tailing_dam_id", nullable = false)
    private UUID tailingDamId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    private BigDecimal capacity;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelaveStatus status;

    protected Relave() {
    }

    public Relave(
            UUID id,
            String name,
            UUID tailingDamId,
            UUID organizationId,
            BigDecimal capacity,
            BigDecimal latitude,
            BigDecimal longitude,
            String address
    ) {
        this.id = id;
        this.name = name;
        this.tailingDamId = tailingDamId;
        this.organizationId = organizationId;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = RelaveStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getTailingDamId() {
        return tailingDamId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public RelaveStatus getStatus() {
        return status;
    }
}
