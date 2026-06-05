package com.chicamax.sentinella.monitoring.bootstrap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Mantiene lecturas demo dentro de la ventana del dashboard (última hora).
 * Sin esto, los KPI de cobertura bajan hasta 0 cuando envejecen los timestamps del seed.
 */
@Component
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class DemoTelemetryRefreshScheduler {

    private final MonitoringDemoDataSeeder seeder;
    private final TransactionTemplate transactionTemplate;

    public DemoTelemetryRefreshScheduler(
            MonitoringDemoDataSeeder seeder,
            PlatformTransactionManager transactionManager
    ) {
        this.seeder = seeder;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedRateString = "${sentinella.seed.refresh-ms:900000}")
    public void refreshRecentReadings() {
        transactionTemplate.executeWithoutResult(status -> seeder.refreshRecentReadings());
    }
}
