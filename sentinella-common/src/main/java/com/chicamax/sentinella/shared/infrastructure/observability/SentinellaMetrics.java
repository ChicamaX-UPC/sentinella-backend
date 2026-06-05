package com.chicamax.sentinella.shared.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SentinellaMetrics {

    private static final String IOT_RECEIVED = "iot_messages_received_total";
    private static final String IOT_PROCESSING = "iot_processing_latency_seconds";
    private static final String ALERT_CREATED = "alert_creation_total";

    private final MeterRegistry registry;
    private final Timer telemetryProcessing;

    public SentinellaMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.telemetryProcessing = Timer.builder(IOT_PROCESSING)
                .description("Proceso de evaluacion de reglas de umbral al consumir telemetria")
                .register(registry);
    }

    public void recordIotMessageAccepted(UUID nodeId, String sensorType) {
        registry.counter(
                IOT_RECEIVED,
                "node", nodeId.toString(),
                "sensor_type", sensorType
        ).increment();
    }

    public void recordTelemetryProcessing(Runnable work) {
        telemetryProcessing.record(work);
    }

    public void recordAlertCreated(String severity) {
        registry.counter(ALERT_CREATED, "severity", severity).increment();
    }
}
