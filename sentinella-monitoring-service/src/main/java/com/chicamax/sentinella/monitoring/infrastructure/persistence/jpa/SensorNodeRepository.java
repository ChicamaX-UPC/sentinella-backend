package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorNodeRepository extends JpaRepository<SensorNode, UUID> {

    @Query("""
            SELECT n FROM SensorNode n
            WHERE (:scoped = false OR n.tailingDamId IN :damIds)
            ORDER BY n.name ASC, n.id ASC
            """)
    Page<SensorNode> findPaged(
            @Param("scoped") boolean scoped,
            @Param("damIds") Collection<UUID> damIds,
            Pageable pageable
    );
}
