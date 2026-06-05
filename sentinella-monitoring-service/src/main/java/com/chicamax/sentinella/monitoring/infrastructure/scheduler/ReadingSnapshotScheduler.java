package com.chicamax.sentinella.monitoring.infrastructure.scheduler;

import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReadingSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReadingSnapshotScheduler.class);

    private final EntityManager entityManager;
    private final ReadingSnapshotRepository readingSnapshotRepository;

    public ReadingSnapshotScheduler(EntityManager entityManager, ReadingSnapshotRepository readingSnapshotRepository) {
        this.entityManager = entityManager;
        this.readingSnapshotRepository = readingSnapshotRepository;
    }

    @Scheduled(fixedRateString = "${sentinella.monitoring.snapshot-interval-ms:900000}")
    @Transactional
    public void aggregateSnapshots() {
        OffsetDateTime bucketStart = OffsetDateTime.now().truncatedTo(ChronoUnit.MINUTES).minusMinutes(15);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT node_id, sensor_type,
                               AVG(value), MIN(value), MAX(value), COUNT(*)
                        FROM monitoring.sensor_readings
                        WHERE timestamp >= :from
                        GROUP BY node_id, sensor_type
                        """)
                .setParameter("from", bucketStart)
                .getResultList();

        for (Object[] row : rows) {
            readingSnapshotRepository.save(new ReadingSnapshot(
                    UUID.randomUUID(),
                    UUID.fromString(row[0].toString()),
                    String.valueOf(row[1]),
                    bucketStart,
                    toDecimal(row[2]),
                    toDecimal(row[3]),
                    toDecimal(row[4]),
                    ((Number) row[5]).intValue()
            ));
        }
        if (!rows.isEmpty()) {
            log.debug("sentinella.monitoring: {} snapshots agregados desde {}", rows.size(), bucketStart);
        }
    }

    private static BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
