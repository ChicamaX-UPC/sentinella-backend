package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
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

    @Query("SELECT n.id FROM SensorNode n WHERE n.tailingDamId IN :damIds")
    List<UUID> findIdsByTailingDamIdIn(@Param("damIds") Collection<UUID> damIds);

    @Query("""
            SELECT COUNT(n) FROM SensorNode n
            WHERE (:scoped = false OR n.tailingDamId IN :damIds)
            """)
    long countScoped(@Param("scoped") boolean scoped, @Param("damIds") Collection<UUID> damIds);

    List<SensorNode> findBySensorTypeIn(Collection<SensorType> sensorTypes);

    List<SensorNode> findByTailingDamIdIn(Collection<UUID> tailingDamIds);

    @Query("""
            SELECT n FROM SensorNode n
            WHERE COALESCE(n.lastSeen, n.createdAt) < :threshold
            """)
    List<SensorNode> findPotentiallyOffline(@Param("threshold") OffsetDateTime threshold);

    @Query("""
            SELECT n FROM SensorNode n
            WHERE n.id IN :ids AND COALESCE(n.lastSeen, n.createdAt) >= :threshold
            """)
    List<SensorNode> findOnlineAmong(
            @Param("ids") Collection<UUID> ids,
            @Param("threshold") OffsetDateTime threshold
    );
}
