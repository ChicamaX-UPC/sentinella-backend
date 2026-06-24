package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThresholdRuleRepository extends JpaRepository<ThresholdRule, UUID> {

    List<ThresholdRule> findByNodeIdAndActiveTrue(UUID nodeId);

    List<ThresholdRule> findByNodeIdIn(Collection<UUID> nodeIds);

    Page<ThresholdRule> findByNodeIdIn(Collection<UUID> nodeIds, Pageable pageable);
}
