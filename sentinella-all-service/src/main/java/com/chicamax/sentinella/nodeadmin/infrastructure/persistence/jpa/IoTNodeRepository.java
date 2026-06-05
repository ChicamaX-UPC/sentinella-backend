package com.chicamax.sentinella.nodeadmin.infrastructure.persistence.jpa;

import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IoTNodeRepository extends JpaRepository<IoTNode, UUID> {
    List<IoTNode> findByTailingDamId(UUID tailingDamId);

    boolean existsByExternalId(String externalId);
}
