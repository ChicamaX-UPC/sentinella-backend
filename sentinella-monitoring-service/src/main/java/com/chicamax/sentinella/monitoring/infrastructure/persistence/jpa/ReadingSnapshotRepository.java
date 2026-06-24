package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingSnapshotRepository extends JpaRepository<ReadingSnapshot, UUID> {

    List<ReadingSnapshot> findTop48ByNodeIdOrderByBucketStartDesc(UUID nodeId);

    List<ReadingSnapshot> findByNodeIdAndBucketStartAfterOrderByBucketStartAsc(UUID nodeId, OffsetDateTime after);

    @Query("""
            SELECT AVG(s.avgValue)
            FROM ReadingSnapshot s
            WHERE s.sensorType = :sensorType
              AND s.bucketStart >= :from
            """)
    Double averageBySensorTypeSince(@Param("sensorType") String sensorType, @Param("from") OffsetDateTime from);
}
