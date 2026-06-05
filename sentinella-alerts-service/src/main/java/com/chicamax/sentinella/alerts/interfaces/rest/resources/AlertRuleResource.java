package com.chicamax.sentinella.alerts.interfaces.rest.resources;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertRuleResource(
        UUID id,
        UUID nodeId,
        String sensorType,
        AlertRuleOperator operator,
        BigDecimal thresholdValue,
        AlertSeverity severity,
        AlertChannel[] channels,
        Integer escalationMinutes,
        boolean active,
        UUID updatedBy,
        OffsetDateTime updatedAt
) {
}
