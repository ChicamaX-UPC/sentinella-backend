package com.chicamax.sentinella.subscriptions.infrastructure.scheduling;

import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionBillingScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionBillingScheduler.class);

    private final SubscriptionCommandService subscriptionCommandService;

    public SubscriptionBillingScheduler(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;
    }

    @Scheduled(cron = "${sentinella.billing.scheduler-cron:0 0 * * * *}")
    public void runBillingLifecycle() {
        try {
            subscriptionCommandService.runBillingLifecycle();
        } catch (Exception ex) {
            log.error("Error en ciclo de facturación: {}", ex.getMessage(), ex);
        }
    }
}
