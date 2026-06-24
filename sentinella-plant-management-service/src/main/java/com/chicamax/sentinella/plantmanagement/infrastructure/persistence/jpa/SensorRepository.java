package com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Sensor;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    List<Sensor> findByTailingDamId(UUID tailingDamId);

    long countByTailingDamIdIn(Collection<UUID> tailingDamIds);

    boolean existsByExternalId(String externalId);
}
