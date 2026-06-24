package com.chicamax.sentinella.iam.domain.model.aggregates;

import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "organizations", schema = "iam")
public class Organization extends AuditableAbstractAggregateRoot<Organization> {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    protected Organization() {
    }

    public Organization(UUID id, String name) {
        this.id = id;
        this.name = name.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
