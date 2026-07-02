package com.chicamax.sentinella.monitoring.bootstrap;

import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import java.math.BigDecimal;
import java.util.List;
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

    /** Umbral legado con unidad incoherente (85.0 sobre cotas ~782 msnm): se corrige a msnm. */
    private static final BigDecimal LEGACY_WATER_LEVEL_THRESHOLD = new BigDecimal("85.0");
    private static final String WATER_LEVEL_HIGH_MSNM = "784.5";
    private static final String WATER_LEVEL_CRITICAL_MSNM = "786.0";

    private final ThresholdRuleRepository thresholdRuleRepository;

    public ThresholdRulesDemoSeeder(ThresholdRuleRepository thresholdRuleRepository) {
        this.thresholdRuleRepository = thresholdRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (thresholdRuleRepository.count() == 0) {
            seedRule(SentinellaDemoIds.NODE_WATER_LEVEL, "water_level", ThresholdRuleOperator.GTE, WATER_LEVEL_HIGH_MSNM, "HIGH");
            seedRule(SentinellaDemoIds.NODE_PRESSURE, "pressure", ThresholdRuleOperator.GT, "120.0", "CRITICAL");
            seedRule(SentinellaDemoIds.NODE_PLUVIOMETER, "pluviometer", ThresholdRuleOperator.GTE, "45.0", "MEDIUM");
            log.info("sentinella.seed (monitoring): reglas de umbral demo creadas.");
        } else {
            fixLegacyWaterLevelRule();
        }
        ensureTwinRules();
    }

    /**
     * Corrige la regla demo de nivel con umbral 85.0 (unidad incorrecta frente a cotas en msnm):
     * disparaba SIEMPRE con lecturas ~782 msnm.
     */
    private void fixLegacyWaterLevelRule() {
        List<ThresholdRule> rules = thresholdRuleRepository
                .findByNodeIdAndActiveTrue(SentinellaDemoIds.NODE_WATER_LEVEL);
        for (ThresholdRule rule : rules) {
            if ("water_level".equalsIgnoreCase(rule.getSensorType())
                    && rule.getThresholdValue().compareTo(LEGACY_WATER_LEVEL_THRESHOLD) == 0) {
                rule.update(
                        rule.getSensorType(),
                        rule.getOperator(),
                        new BigDecimal(WATER_LEVEL_HIGH_MSNM),
                        rule.getSeverity(),
                        rule.decodeChannels(),
                        rule.getEscalationMinutes(),
                        rule.isActive(),
                        rule.getUpdatedBy()
                );
                thresholdRuleRepository.save(rule);
                log.info(
                        "sentinella.seed (monitoring): regla water_level corregida de {} a {} msnm.",
                        LEGACY_WATER_LEVEL_THRESHOLD,
                        WATER_LEVEL_HIGH_MSNM
                );
            }
        }
    }

    /**
     * Reglas de umbral para los nodos del gemelo digital (NW-01, PI-01…): con ellas la
     * telemetría simulada del gemelo dispara alertas reales al desbordar o saturar el dique.
     * Idempotente: solo crea las que faltan para cada nodo/tipo.
     */
    private void ensureTwinRules() {
        int created = 0;
        created += ensureRule("NW-01", "water_level", ThresholdRuleOperator.GTE, WATER_LEVEL_HIGH_MSNM, "HIGH");
        created += ensureRule("NW-01", "water_level", ThresholdRuleOperator.GTE, WATER_LEVEL_CRITICAL_MSNM, "CRITICAL");
        for (String pi : List.of("PI-01", "PI-02", "PI-03", "PI-04", "PI-05")) {
            created += ensureRule(pi, "pressure", ThresholdRuleOperator.GT, "120.0", "CRITICAL");
        }
        for (String in : List.of("IN-01", "IN-02", "IN-03")) {
            created += ensureRule(in, "inclination", ThresholdRuleOperator.GTE, "0.40", "HIGH");
        }
        created += ensureRule("PV-01", "pluviometer", ThresholdRuleOperator.GTE, "45.0", "HIGH");
        if (created > 0) {
            log.info("sentinella.seed (monitoring): {} reglas de umbral del gemelo digital creadas.", created);
        }
    }

    private int ensureRule(
            String twinExternalId,
            String sensorType,
            ThresholdRuleOperator operator,
            String threshold,
            String severity
    ) {
        UUID nodeId = SentinellaDemoIds.twinNodeId(twinExternalId);
        BigDecimal value = new BigDecimal(threshold);
        boolean exists = thresholdRuleRepository.findByNodeIdAndActiveTrue(nodeId).stream()
                .anyMatch(rule -> sensorType.equalsIgnoreCase(rule.getSensorType())
                        && rule.getOperator() == operator
                        && rule.getThresholdValue().compareTo(value) == 0);
        if (exists) {
            return 0;
        }
        thresholdRuleRepository.save(ThresholdRule.create(
                UUID.randomUUID(),
                nodeId,
                sensorType,
                operator,
                value,
                severity
        ));
        return 1;
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
