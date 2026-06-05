package com.chicamax.sentinella.shared.infrastructure.messaging;

import com.chicamax.sentinella.monitoring.domain.services.SensorReadingCommandService;
import com.chicamax.sentinella.monitoring.infrastructure.messaging.SensorReadingRabbitMapper;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component
public class TelemetryIngestionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryIngestionConsumer.class);

    private final SensorReadingRabbitMapper mapper;
    private final SensorReadingCommandService sensorReadingCommandService;
    private final ThresholdEvaluationService thresholdEvaluationService;
    private final SentinellaMetrics sentinellaMetrics;

    public TelemetryIngestionConsumer(
            SensorReadingRabbitMapper mapper,
            SensorReadingCommandService sensorReadingCommandService,
            ThresholdEvaluationService thresholdEvaluationService,
            SentinellaMetrics sentinellaMetrics
    ) {
        this.mapper = mapper;
        this.sensorReadingCommandService = sensorReadingCommandService;
        this.thresholdEvaluationService = thresholdEvaluationService;
        this.sentinellaMetrics = sentinellaMetrics;
    }

    @RabbitListener(queues = "telemetry.queue")
    public void onTelemetry(SensorReadingReceivedMessage message) {
        try {
            sentinellaMetrics.recordTelemetryProcessing(() -> {
                var command = mapper.toCommand(message);
                sensorReadingCommandService.handle(command);
                thresholdEvaluationService.evaluateInternal(message);
            });
        } catch (IllegalArgumentException ex) {
            log.warn("Telemetria descartada: {}", ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Fallo al procesar telemetria", ex);
            throw ex;
        }
    }
}
