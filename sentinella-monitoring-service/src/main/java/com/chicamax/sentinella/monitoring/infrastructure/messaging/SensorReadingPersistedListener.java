package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingPersistedListener {

    private final ThresholdEvaluationService thresholdEvaluationService;
    private final SentinellaMetrics sentinellaMetrics;

    public SensorReadingPersistedListener(
            ThresholdEvaluationService thresholdEvaluationService,
            SentinellaMetrics sentinellaMetrics
    ) {
        this.thresholdEvaluationService = thresholdEvaluationService;
        this.sentinellaMetrics = sentinellaMetrics;
    }

    @RabbitListener(queues = "sensor.reading.persisted.queue")
    public void onPersistedReading(SensorReadingReceivedMessage message) {
        sentinellaMetrics.recordTelemetryProcessing(() -> thresholdEvaluationService.evaluate(message));
    }
}
