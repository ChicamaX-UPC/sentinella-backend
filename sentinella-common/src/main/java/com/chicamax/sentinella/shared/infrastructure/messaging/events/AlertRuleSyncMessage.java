package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Sincronización de reglas de umbral Monitoring → Alerts.
 *
 * <p>Monitoring es el dueño de las reglas ({@code threshold_rules}); Alerts mantiene una réplica
 * ({@code alert_rules}) porque su tabla {@code alerts} referencia la regla por FK. Este evento
 * transporta la regla (con su id estable) para que Alerts haga upsert y pueda crear alertas.
 */
public record AlertRuleSyncMessage(
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        String operator,
        BigDecimal thresholdValue,
        String severity,
        String channels,
        Integer escalationMinutes,
        boolean active
) {
}
