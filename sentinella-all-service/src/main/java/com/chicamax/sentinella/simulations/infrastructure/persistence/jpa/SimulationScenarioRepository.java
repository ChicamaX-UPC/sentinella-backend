package com.chicamax.sentinella.simulations.infrastructure.persistence.jpa;

import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationScenarioRepository extends JpaRepository<SimulationScenario, UUID> {

    List<SimulationScenario> findByTailingDamIdInAndCreatedBy(Collection<UUID> tailingDamIds, UUID createdBy);

    List<SimulationScenario> findByTailingDamIdInAndIsPublicIsTrue(Collection<UUID> tailingDamIds);

    List<SimulationScenario> findByCreatedBy(UUID createdBy);

    List<SimulationScenario> findByIsPublicTrue();

    List<SimulationScenario> findByIsPublicTrueAndTailingDamIdIn(Collection<UUID> tailingDamIds);
}
