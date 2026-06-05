package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading.SensorReadingId;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, SensorReadingId> {

    Optional<SensorReading> findTopByNodeIdOrderByTimestampDesc(UUID nodeId);

    Optional<SensorReading> findTopByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(UUID nodeId, OffsetDateTime from);

    Page<SensorReading> findByNodeIdOrderByTimestampDesc(UUID nodeId, Pageable pageable);

    Page<SensorReading> findByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(
            UUID nodeId,
            OffsetDateTime from,
            Pageable pageable
    );

    Page<SensorReading> findByNodeIdAndTimestampLessThanEqualOrderByTimestampDesc(
            UUID nodeId,
            OffsetDateTime to,
            Pageable pageable
    );

    Page<SensorReading> findByNodeIdAndTimestampBetweenOrderByTimestampDesc(
            UUID nodeId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );
}
