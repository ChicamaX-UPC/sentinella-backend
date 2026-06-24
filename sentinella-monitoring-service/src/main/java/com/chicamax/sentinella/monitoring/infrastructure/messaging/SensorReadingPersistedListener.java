package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingPersistedListener {

    private final ThresholdEvaluationService thresholdEvaluationService;
    private final SentinellaMetrics sentinellaMetrics;
    private final DashboardKpiRecomputePublisher dashboardKpiRecomputePublisher;

    public SensorReadingPersistedListener(
            ThresholdEvaluationService thresholdEvaluationService,
            SentinellaMetrics sentinellaMetrics,
            DashboardKpiRecomputePublisher dashboardKpiRecomputePublisher
    ) {
        this.thresholdEvaluationService = thresholdEvaluationService;
        this.sentinellaMetrics = sentinellaMetrics;
        this.dashboardKpiRecomputePublisher = dashboardKpiRecomputePublisher;
    }

    @RabbitListener(queues = "sensor.reading.persisted.queue")
    public void onPersistedReading(SensorReadingReceivedMessage message) {
        sentinellaMetrics.recordTelemetryProcessing(() -> thresholdEvaluationService.evaluate(message));
        dashboardKpiRecomputePublisher.publishThrottled("sensor.reading", message.nodeId());
    }
}
