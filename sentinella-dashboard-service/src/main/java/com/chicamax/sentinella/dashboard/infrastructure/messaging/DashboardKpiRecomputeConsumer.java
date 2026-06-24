package com.chicamax.sentinella.dashboard.infrastructure.messaging;

import com.chicamax.sentinella.dashboard.application.internal.cache.ExecutiveKpiCache;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.DashboardKpiRecomputeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DashboardKpiRecomputeConsumer {

    private final ExecutiveKpiCache executiveKpiCache;

    public DashboardKpiRecomputeConsumer(ExecutiveKpiCache executiveKpiCache) {
        this.executiveKpiCache = executiveKpiCache;
    }

    @RabbitListener(queues = "dashboard.kpi.recompute.queue")
    public void onRecompute(DashboardKpiRecomputeMessage message) {
        executiveKpiCache.invalidateAll();
    }
}
