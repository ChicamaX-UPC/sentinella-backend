package com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa;

import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading.SensorReadingId;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, SensorReadingId> {

    Optional<SensorReading> findTopByNodeIdOrderByTimestampDesc(UUID nodeId);

    Optional<SensorReading> findTopByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(UUID nodeId, OffsetDateTime from);

    List<SensorReading> findByNodeIdOrderByTimestampDesc(UUID nodeId, Pageable pageable);

    List<SensorReading> findByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(
            UUID nodeId,
            OffsetDateTime from,
            Pageable pageable
    );

    List<SensorReading> findByNodeIdAndTimestampLessThanEqualOrderByTimestampDesc(
            UUID nodeId,
            OffsetDateTime to,
            Pageable pageable
    );

    List<SensorReading> findByNodeIdAndTimestampBetweenOrderByTimestampDesc(
            UUID nodeId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );
}
