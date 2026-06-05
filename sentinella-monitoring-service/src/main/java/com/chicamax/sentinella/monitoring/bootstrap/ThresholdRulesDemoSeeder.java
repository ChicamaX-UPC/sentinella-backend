package com.chicamax.sentinella.monitoring.bootstrap;

import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(110)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class ThresholdRulesDemoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ThresholdRulesDemoSeeder.class);

    private final ThresholdRuleRepository thresholdRuleRepository;

    public ThresholdRulesDemoSeeder(ThresholdRuleRepository thresholdRuleRepository) {
        this.thresholdRuleRepository = thresholdRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (thresholdRuleRepository.count() > 0) {
            return;
        }
        seedRule(SentinellaDemoIds.NODE_WATER_LEVEL, "water_level", ThresholdRuleOperator.GTE, "85.0", "HIGH");
        seedRule(SentinellaDemoIds.NODE_PRESSURE, "pressure", ThresholdRuleOperator.GT, "120.0", "CRITICAL");
        seedRule(SentinellaDemoIds.NODE_PLUVIOMETER, "pluviometer", ThresholdRuleOperator.GTE, "45.0", "MEDIUM");
        log.info("sentinella.seed (monitoring): reglas de umbral demo creadas.");
    }

    private void seedRule(
            UUID nodeId,
            String sensorType,
            ThresholdRuleOperator operator,
            String threshold,
            String severity
    ) {
        thresholdRuleRepository.save(ThresholdRule.create(
                UUID.randomUUID(),
                nodeId,
                sensorType,
                operator,
                new BigDecimal(threshold),
                severity
        ));
    }
}
