package com.chicamax.sentinella.monitoring.application.internal.prediction;

import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingSnapshotBackfillService {

    private static final Logger log = LoggerFactory.getLogger(ReadingSnapshotBackfillService.class);

    private final EntityManager entityManager;
    private final ReadingSnapshotRepository readingSnapshotRepository;

    public ReadingSnapshotBackfillService(
            EntityManager entityManager,
            ReadingSnapshotRepository readingSnapshotRepository
    ) {
        this.entityManager = entityManager;
        this.readingSnapshotRepository = readingSnapshotRepository;
    }

    @Transactional
    public int backfillLastDays(int days) {
        if (readingSnapshotRepository.count() > 0) {
            return 0;
        }
        OffsetDateTime from = OffsetDateTime.now().minusDays(days).truncatedTo(ChronoUnit.HOURS);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT node_id, sensor_type,
                               date_trunc('hour', timestamp)
                                 + (EXTRACT(MINUTE FROM timestamp)::int / 15) * interval '15 minutes' AS bucket_start,
                               AVG(value), MIN(value), MAX(value), COUNT(*)
                        FROM monitoring.sensor_readings
                        WHERE timestamp >= :from
                        GROUP BY node_id, sensor_type, bucket_start
                        ORDER BY bucket_start
                        """)
                .setParameter("from", from)
                .getResultList();

        int saved = 0;
        for (Object[] row : rows) {
            readingSnapshotRepository.save(new ReadingSnapshot(
                    UUID.randomUUID(),
                    UUID.fromString(row[0].toString()),
                    String.valueOf(row[1]),
                    ((java.sql.Timestamp) row[2]).toInstant().atOffset(ZoneOffset.UTC),
                    toDecimal(row[3]),
                    toDecimal(row[4]),
                    toDecimal(row[5]),
                    ((Number) row[6]).intValue()
            ));
            saved++;
        }
        if (saved > 0) {
            log.info("sentinella.prediction: {} snapshots backfilled desde {}", saved, from);
        }
        return saved;
    }

    private static BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
