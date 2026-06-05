package com.chicamax.sentinella.alerts.bootstrap;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertAuditEntryRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import java.math.BigDecimal;
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
public class AlertsDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AlertsDemoDataSeeder.class);
    private static final int DEMO_ALERTS = 85;

    private final AlertRepository alertRepository;
    private final AlertAuditEntryRepository alertAuditEntryRepository;
    private final AlertCommandService alertCommandService;
    private final TransactionTemplate transactionTemplate;
    private final boolean seedForce;

    public AlertsDemoDataSeeder(
            AlertRepository alertRepository,
            AlertAuditEntryRepository alertAuditEntryRepository,
            AlertCommandService alertCommandService,
            PlatformTransactionManager transactionManager,
            @Value("${sentinella.seed.force:false}") boolean seedForce
    ) {
        this.alertRepository = alertRepository;
        this.alertAuditEntryRepository = alertAuditEntryRepository;
        this.alertCommandService = alertCommandService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.seedForce = seedForce;
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        if (!seedForce && alertRepository.count() >= DEMO_ALERTS) {
            log.info("sentinella.seed (alerts): {} alertas demo ya presentes, se omite.", alertRepository.count());
            return;
        }

        if (seedForce && alertRepository.count() > 0) {
            alertAuditEntryRepository.deleteAllInBatch();
            alertRepository.deleteAllInBatch();
        }

        AlertSeverity[] severities = {
                AlertSeverity.WARNING,
                AlertSeverity.CRITICAL,
                AlertSeverity.INFO,
                AlertSeverity.WARNING
        };
        String[] sensorTypes = {"WATER_LEVEL", "PRESSURE", "PLUVIOMETER", "INCLINATION", "PH", "TURBIDITY"};

        for (int a = 0; a < DEMO_ALERTS; a++) {
            alertCommandService.create(new CreateAlertCommand(
                    null,
                    SentinellaDemoIds.demoNodeId(a % 40),
                    sensorTypes[a % sensorTypes.length],
                    triggeredValueFor(sensorTypes[a % sensorTypes.length], a),
                    severities[a % severities.length],
                    "APP,EMAIL",
                    SentinellaDemoIds.USER_ADMIN,
                    "SYSTEM_ADMIN"
            ));
        }

        log.info("sentinella.seed (alerts): {} alertas demo creadas (umbrales en Monitoring).", DEMO_ALERTS);
    }

    private static BigDecimal triggeredValueFor(String sensorType, int index) {
        return switch (sensorType) {
            case "WATER_LEVEL" -> BigDecimal.valueOf(82 + (index % 12));
            case "PRESSURE" -> BigDecimal.valueOf(110 + (index % 25));
            case "PLUVIOMETER" -> BigDecimal.valueOf(40 + (index % 20));
            default -> BigDecimal.valueOf(50 + (index % 30));
        };
    }
}
