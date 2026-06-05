package com.chicamax.sentinella.shared.infrastructure.observability;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.MultiGauge.Row;
import io.micrometer.core.instrument.Tags;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NodeOfflineDurationMetrics {

    private static final String METRIC = "node_offline_duration_seconds";

    private final SensorNodeRepository sensorNodeRepository;
    private final MultiGauge multiGauge;
    private final boolean enabled;

    public NodeOfflineDurationMetrics(
            MeterRegistry registry,
            SensorNodeRepository sensorNodeRepository,
            @Value("${observability.node-offline-metric.enabled:true}") boolean enabled
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.enabled = enabled;
        this.multiGauge = MultiGauge.builder(METRIC)
                .description("Segundos transcurridos desde la ultima lectura (last_seen) por nodo")
                .register(registry);
    }

    @Scheduled(fixedDelayString = "${observability.node-offline-metric.fixed-delay-ms:30000}")
    public void refresh() {
        if (!enabled) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<Row<?>> rows = new ArrayList<>();
        for (SensorNode n : sensorNodeRepository.findAll()) {
            OffsetDateTime ref = n.getLastSeen() != null ? n.getLastSeen() : n.getCreatedAt();
            if (ref == null) {
                continue;
            }
            long seconds = Duration.between(ref, now).getSeconds();
            if (seconds < 0) {
                seconds = 0;
            }
            rows.add(Row.of(
                    Tags.of("node", n.getId().toString(), "tailing_dam", n.getTailingDamId().toString()),
                    seconds
            ));
        }
        multiGauge.register(rows, true);
    }
}
