package com.chicamax.sentinella.reports.domain.model.aggregates;

import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", schema = "reports")
public class Report extends AuditableAbstractAggregateRoot<Report> {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportFormat format;

    @Column(name = "tailing_dam_id")
    private UUID tailingDamId;

    @Column(name = "from_date", nullable = false)
    private OffsetDateTime fromDate;

    @Column(name = "to_date", nullable = false)
    private OffsetDateTime toDate;

    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    protected Report() {
    }

    public Report(
            UUID id,
            ReportType type,
            ReportFormat format,
            UUID tailingDamId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            UUID generatedBy,
            String storageKey
    ) {
        this.id = id;
        this.type = type;
        this.format = format;
        this.tailingDamId = tailingDamId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.generatedBy = generatedBy;
        this.storageKey = storageKey;
    }

    public UUID getId() {
        return id;
    }

    public ReportType getType() {
        return type;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public UUID getTailingDamId() {
        return tailingDamId;
    }

    public OffsetDateTime getFromDate() {
        return fromDate;
    }

    public OffsetDateTime getToDate() {
        return toDate;
    }

    public UUID getGeneratedBy() {
        return generatedBy;
    }

    public String getStorageKey() {
        return storageKey;
    }
}
