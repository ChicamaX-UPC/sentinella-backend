package com.chicamax.sentinella.simulations.domain.model.aggregates;

import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
@Table(name = "simulation_scenarios", schema = "simulations")
public class SimulationScenario extends AuditableAbstractAggregateRoot<SimulationScenario> {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "simulation_type", nullable = false, length = 50)
    private SimulationType simulationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String parameters;

    @Column(name = "tailing_dam_id", nullable = false)
    private UUID tailingDamId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    protected SimulationScenario() {
    }

    public SimulationScenario(
            UUID id,
            String name,
            String description,
            SimulationType simulationType,
            String parameters,
            UUID tailingDamId,
            UUID createdBy
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.simulationType = simulationType;
        this.parameters = parameters;
        this.tailingDamId = tailingDamId;
        this.createdBy = createdBy;
        this.isPublic = false;
    }

    public void updateContent(String name, String description, SimulationType simulationType, String parameters) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (simulationType != null) {
            this.simulationType = simulationType;
        }
        if (parameters != null && !parameters.isBlank()) {
            this.parameters = parameters;
        }
    }

    public void publish() {
        this.isPublic = true;
    }

    public void unpublish() {
        this.isPublic = false;
    }

    public void assertCreatorOrSystemAdmin(UUID actorId, String role) {
        if ("SYSTEM_ADMIN".equals(role)) {
            return;
        }
        if (!createdBy.equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el creador o SYSTEM_ADMIN puede modificar este escenario");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SimulationType getSimulationType() {
        return simulationType;
    }

    public String getParameters() {
        return parameters;
    }

    public UUID getTailingDamId() {
        return tailingDamId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
