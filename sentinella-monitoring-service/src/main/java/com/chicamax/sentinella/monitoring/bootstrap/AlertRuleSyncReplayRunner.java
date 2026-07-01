package com.chicamax.sentinella.monitoring.bootstrap;

import com.chicamax.sentinella.monitoring.infrastructure.messaging.AlertRuleSyncPublisher;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Replica todas las reglas de umbral hacia Alerts al arrancar (tras los seeders, @Order 100/110).
 *
 * <p>Necesario porque las reglas demo se siembran con ids aleatorios y Alerts debe conocerlas para
 * el FK de sus alertas. La cola {@code alert.rule.sync.queue} es durable: si Alerts está caído, los
 * mensajes se conservan. El consumidor hace upsert, así que reenviarlas en cada arranque es seguro.
 */
@Component
@Order(120)
public class AlertRuleSyncReplayRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AlertRuleSyncReplayRunner.class);

    private final ThresholdRuleRepository thresholdRuleRepository;
    private final AlertRuleSyncPublisher publisher;

    public AlertRuleSyncReplayRunner(
            ThresholdRuleRepository thresholdRuleRepository,
            AlertRuleSyncPublisher publisher
    ) {
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) {
        var rules = thresholdRuleRepository.findAll();
        rules.forEach(publisher::publish);
        if (!rules.isEmpty()) {
            log.info("sentinella.sync (monitoring): {} reglas de umbral replicadas a alerts.", rules.size());
        }
    }
}
