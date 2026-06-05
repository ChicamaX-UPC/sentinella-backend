package com.chicamax.sentinella.fieldoperations.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "checklist_items", schema = "plant_management")
public class ChecklistItem {

    @Id
    private UUID id;

    @Column(name = "round_id", nullable = false)
    private UUID roundId;

    @Column(name = "point_name", nullable = false)
    private String pointName;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column
    private String observations;

    @Column(name = "photo_s3_key")
    private String photoS3Key;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "is_anomaly", nullable = false)
    private boolean anomaly;

    protected ChecklistItem() {
    }

    public ChecklistItem(UUID id, UUID roundId, String pointName, boolean required) {
        this.id = id;
        this.roundId = roundId;
        this.pointName = pointName;
        this.required = required;
    }

    public void complete(String observations, String photoS3Key, Double latitude, Double longitude, boolean anomaly) {
        this.observations = observations;
        this.photoS3Key = photoS3Key;
        this.latitude = latitude == null ? null : BigDecimal.valueOf(latitude);
        this.longitude = longitude == null ? null : BigDecimal.valueOf(longitude);
        this.anomaly = anomaly;
        this.completedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public String getPointName() {
        return pointName;
    }

    public boolean isRequired() {
        return required;
    }

    public String getObservations() {
        return observations;
    }

    public String getPhotoS3Key() {
        return photoS3Key;
    }

    public Double getLatitude() {
        return latitude == null ? null : latitude.doubleValue();
    }

    public Double getLongitude() {
        return longitude == null ? null : longitude.doubleValue();
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public boolean isAnomaly() {
        return anomaly;
    }
}
