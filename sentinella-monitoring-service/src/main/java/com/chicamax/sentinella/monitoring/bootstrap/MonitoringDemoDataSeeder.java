package com.chicamax.sentinella.monitoring.bootstrap;

import com.chicamax.sentinella.monitoring.application.internal.prediction.ReadingSnapshotBackfillService;
import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Order(100)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class MonitoringDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MonitoringDemoDataSeeder.class);

    private static final int DEMO_NODE_COUNT = 40;
    private static final int HISTORY_DAYS = 90;
    private static final int READING_EVERY_HOURS = 3;
    private static final int BATCH_SIZE = 400;
    private static final int MIN_NODES_FOR_SKIP = 30;

    /** Centro aproximado del tranque Chicama Norte (relaves). */
    private static final double DAM_CENTER_LAT = -7.9212;
    private static final double DAM_CENTER_LNG = -78.5140;

    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final ReadingSnapshotRepository readingSnapshotRepository;
    private final ReadingSnapshotBackfillService readingSnapshotBackfillService;
    private final TransactionTemplate transactionTemplate;
    private final boolean seedForce;

    public MonitoringDemoDataSeeder(
            SensorNodeRepository sensorNodeRepository,
            SensorReadingRepository sensorReadingRepository,
            ReadingSnapshotRepository readingSnapshotRepository,
            ReadingSnapshotBackfillService readingSnapshotBackfillService,
            PlatformTransactionManager transactionManager,
            @Value("${sentinella.seed.force:false}") boolean seedForce
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.readingSnapshotRepository = readingSnapshotRepository;
        this.readingSnapshotBackfillService = readingSnapshotBackfillService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.seedForce = seedForce;
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        long existingNodes = sensorNodeRepository.count();
        if (!seedForce && existingNodes >= MIN_NODES_FOR_SKIP) {
            repositionDemoNodes();
            ensureRecentReadings();
            log.info(
                    "sentinella.seed (monitoring): {} nodos demo ya cargados, lecturas recientes verificadas.",
                    existingNodes
            );
            return;
        }

        if (seedForce || existingNodes > 0) {
            log.info("sentinella.seed (monitoring): recreando dataset demo (force={}).", seedForce);
            sensorReadingRepository.deleteAllInBatch();
            sensorNodeRepository.deleteAllInBatch();
            readingSnapshotRepository.deleteAllInBatch();
        }

        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<DemoNodeSpec> specs = buildNodeSpecs();
        List<SensorNode> nodes = new ArrayList<>(specs.size());
        for (DemoNodeSpec spec : specs) {
            nodes.add(new SensorNode(
                    spec.id(),
                    spec.externalId(),
                    spec.name(),
                    SentinellaDemoIds.TAILING_DAM_CHICAMA_NORTE,
                    spec.type(),
                    spec.latitude(),
                    spec.longitude(),
                    spec.position3d(),
                    "ONLINE",
                    now.minusHours(spec.index() % 6)
            ));
        }
        sensorNodeRepository.saveAll(nodes);

        OffsetDateTime rangeStart = now.minusDays(HISTORY_DAYS).truncatedTo(ChronoUnit.HOURS);
        List<SensorReading> buffer = new ArrayList<>(BATCH_SIZE);
        int totalReadings = 0;

        for (DemoNodeSpec spec : specs) {
            OffsetDateTime ts = rangeStart;
            int step = 0;
            while (!ts.isAfter(now)) {
                buffer.add(buildReading(spec, ts, step));
                step++;
                if (buffer.size() >= BATCH_SIZE) {
                    sensorReadingRepository.saveAll(buffer);
                    totalReadings += buffer.size();
                    buffer.clear();
                }
                ts = ts.plusHours(READING_EVERY_HOURS);
            }
        }
        if (!buffer.isEmpty()) {
            sensorReadingRepository.saveAll(buffer);
            totalReadings += buffer.size();
        }

        log.info(
                "sentinella.seed (monitoring): {} nodos y {} lecturas demo (ultimos {} dias, cada {} h).",
                specs.size(),
                totalReadings,
                HISTORY_DAYS,
                READING_EVERY_HOURS
        );
        readingSnapshotBackfillService.backfillLastDays(HISTORY_DAYS);
    }

    /**
     * Inserta una lectura nueva por nodo con timestamp casi actual (ventana de 1 h del dashboard).
     * Llamado al arranque y cada {@code sentinella.seed.refresh-ms} por {@link DemoTelemetryRefreshScheduler}.
     */
    public void refreshRecentReadings() {
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(1);
        List<DemoNodeSpec> specs = buildNodeSpecs();
        long nodesWithRecent = specs.stream()
                .filter(spec -> sensorReadingRepository
                        .findTopByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(spec.id(), threshold)
                        .isPresent())
                .count();
        if (nodesWithRecent >= specs.size()) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<SensorReading> recent = new ArrayList<>(specs.size());
        for (DemoNodeSpec spec : specs) {
            recent.add(buildReading(spec, now.minusSeconds(spec.index() % 15L), 0));
        }
        sensorReadingRepository.saveAll(recent);
        log.info(
                "sentinella.seed (monitoring): lecturas recientes para {} nodos (tenian {} dentro de la ultima hora).",
                specs.size(),
                nodesWithRecent
        );
    }

    private void ensureRecentReadings() {
        refreshRecentReadings();
    }

    /** Reubica nodos demo si ya existen (p. ej. tras seed en cuadrícula). */
    private void repositionDemoNodes() {
        List<DemoNodeSpec> specs = buildNodeSpecs();
        int updated = 0;
        for (DemoNodeSpec spec : specs) {
            var nodeOpt = sensorNodeRepository.findById(spec.id());
            if (nodeOpt.isEmpty()) {
                continue;
            }
            SensorNode node = nodeOpt.get();
            if (coordinatesEqual(node.getLatitude(), spec.latitude())
                    && coordinatesEqual(node.getLongitude(), spec.longitude())) {
                continue;
            }
            node.updateGeo(spec.latitude(), spec.longitude());
            sensorNodeRepository.save(node);
            updated++;
        }
        if (updated > 0) {
            log.info(
                    "sentinella.seed (monitoring): {} nodos reubicados en perimetro del tranque (layout realista).",
                    updated
            );
        }
    }

    private static boolean coordinatesEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.setScale(7, RoundingMode.HALF_UP).compareTo(b.setScale(7, RoundingMode.HALF_UP)) == 0;
    }

    private static List<DemoNodeSpec> buildNodeSpecs() {
        List<DemoNodeSpec> specs = new ArrayList<>(DEMO_NODE_COUNT);
        SensorType[] types = SensorType.values();
        String[] prefixes = {"NW", "PI", "PV", "IN", "PH", "TU"};

        for (int i = 0; i < DEMO_NODE_COUNT; i++) {
            UUID id = SentinellaDemoIds.demoNodeId(i);
            SensorType type = types[i % types.length];
            String prefix = prefixes[i % prefixes.length];
            String externalId = "%s-REL-%02d".formatted(prefix, i + 1);
            String name = sensorName(type, i);
            double[] coords = demoCoordinates(i, type);
            BigDecimal lat = bdCoord(coords[0]);
            BigDecimal lng = bdCoord(coords[1]);
            String position3d = i % 4 == 0 ? null : "{\"x\":" + (i * 3) + ",\"y\":" + (i % 10) + ",\"z\":" + (i % 50) + "}";
            specs.add(new DemoNodeSpec(i, id, externalId, name, type, lat, lng, position3d));
        }
        return specs;
    }

    /**
     * Coordenadas alrededor del tranque (elipse + anillos por tipo), no en cuadrícula.
     * Los tres primeros conservan posiciones reales del seed original.
     */
    private static double[] demoCoordinates(int index, SensorType type) {
        if (index == 0) {
            return new double[] {-7.9234, -78.5123};
        }
        if (index == 1) {
            return new double[] {-7.9241, -78.5110};
        }
        if (index == 2) {
            return new double[] {-7.9180, -78.5200};
        }

        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        double angle = index * golden;

        double ring = switch (type) {
            case WATER_LEVEL -> 0.22 + (index % 6) * 0.045;
            case PRESSURE -> 0.52 + (index % 5) * 0.055;
            case PLUVIOMETER -> 0.82 + (index % 4) * 0.04;
            case INCLINATION -> 0.38 + (index % 4) * 0.05;
            case PH, TURBIDITY -> 0.30 + (index % 5) * 0.05;
        };

        double semiLat = 0.0036;
        double semiLng = 0.0050;
        double latOff = Math.sin(angle) * semiLat * ring;
        double lngOff = Math.cos(angle) * semiLng * ring;

        if (type == SensorType.PRESSURE) {
            latOff -= 0.0016;
            lngOff += 0.0010;
        } else if (type == SensorType.PLUVIOMETER) {
            latOff += (index % 3 - 1) * 0.0008;
            lngOff *= 1.08;
        } else if (type == SensorType.WATER_LEVEL) {
            lngOff *= 0.75;
        }

        double jitterLat = Math.sin(index * 12.9898) * 0.00022;
        double jitterLng = Math.cos(index * 78.233) * 0.00024;

        return new double[] {
                DAM_CENTER_LAT + latOff + jitterLat,
                DAM_CENTER_LNG + lngOff + jitterLng
        };
    }

    private static BigDecimal bdCoord(double value) {
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }

    private static String sensorName(SensorType type, int index) {
        return switch (type) {
            case WATER_LEVEL -> "Nivel relave — sector " + (index + 1);
            case PRESSURE -> "Piezometro — talud " + (index + 1);
            case PLUVIOMETER -> "Pluviometro — perimetro " + (index + 1);
            case INCLINATION -> "Inclinometro — cresta " + (index + 1);
            case PH -> "pH — agua de contacto " + (index + 1);
            case TURBIDITY -> "Turbidez — descarga " + (index + 1);
        };
    }

    private static SensorReading buildReading(DemoNodeSpec spec, OffsetDateTime timestamp, int step) {
        BigDecimal value = sampleValue(spec.type(), step);
        SensorStatus status = statusFor(spec.type(), value);
        return new SensorReading(
                UUID.randomUUID(),
                timestamp,
                spec.id(),
                spec.type(),
                value,
                unitFor(spec.type()),
                status,
                "{\"source\":\"seed\"}"
        );
    }

    private static BigDecimal sampleValue(SensorType type, int step) {
        double wave = Math.sin(step / 12.0) * 2.5 + Math.cos(step / 48.0);
        return switch (type) {
            case WATER_LEVEL -> bd(782.0 + wave * 2.2 + (step % 17) * 0.08);
            case PRESSURE -> bd(118.0 + wave * 8.0 + (step % 11));
            case PLUVIOMETER -> bd(Math.max(0, (step % 24 < 3 ? 14.0 : 1.5) + wave));
            case INCLINATION -> bd(0.12 + (step % 9) * 0.01 + wave * 0.02);
            case PH -> bd(7.1 + wave * 0.15);
            case TURBIDITY -> bd(18.0 + wave * 4.0 + (step % 7));
        };
    }

    private static SensorStatus statusFor(SensorType type, BigDecimal value) {
        return switch (type) {
            case WATER_LEVEL -> value.compareTo(new BigDecimal("784.20")) > 0
                    ? SensorStatus.WARNING
                    : SensorStatus.OK;
            case PRESSURE -> value.compareTo(new BigDecimal("145")) > 0
                    ? SensorStatus.CRITICAL
                    : SensorStatus.OK;
            case PLUVIOMETER -> value.compareTo(new BigDecimal("20")) > 0
                    ? SensorStatus.WARNING
                    : SensorStatus.OK;
            default -> SensorStatus.OK;
        };
    }

    private static String unitFor(SensorType type) {
        return switch (type) {
            case WATER_LEVEL -> "msnm";
            case PRESSURE -> "kPa";
            case PLUVIOMETER -> "mm/h";
            case INCLINATION -> "deg";
            case PH -> "pH";
            case TURBIDITY -> "NTU";
        };
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record DemoNodeSpec(
            int index,
            UUID id,
            String externalId,
            String name,
            SensorType type,
            BigDecimal latitude,
            BigDecimal longitude,
            String position3d
    ) {
    }
}
