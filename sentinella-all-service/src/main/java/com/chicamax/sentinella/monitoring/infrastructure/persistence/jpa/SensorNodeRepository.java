package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorNodeRepository extends JpaRepository<SensorNode, UUID> {
}
