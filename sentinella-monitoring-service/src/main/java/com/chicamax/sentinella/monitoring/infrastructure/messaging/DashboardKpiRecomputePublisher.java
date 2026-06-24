package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.DashboardKpiRecomputeMessage;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DashboardKpiRecomputePublisher {

    private static final long THROTTLE_SECONDS = 30;

    private final RabbitTemplate rabbitTemplate;
    private final AtomicReference<Instant> lastPublished = new AtomicReference<>(Instant.EPOCH);

    public DashboardKpiRecomputePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishThrottled(String reason, java.util.UUID nodeId) {
        Instant now = Instant.now();
        Instant last = lastPublished.get();
        if (last.plusSeconds(THROTTLE_SECONDS).isAfter(now)) {
            return;
        }
        if (!lastPublished.compareAndSet(last, now)) {
            return;
        }
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.DASHBOARD_KPI_RECOMPUTE_ROUTING,
                new DashboardKpiRecomputeMessage(reason, nodeId)
        );
    }
}
