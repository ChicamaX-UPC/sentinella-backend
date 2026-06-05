package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, UUID> {
}
