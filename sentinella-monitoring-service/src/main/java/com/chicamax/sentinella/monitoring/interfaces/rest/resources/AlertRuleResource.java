package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdChannel;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdSeverity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertRuleResource(
        UUID id,
        UUID nodeId,
        String sensorType,
        ThresholdRuleOperator operator,
        BigDecimal thresholdValue,
        ThresholdSeverity severity,
        ThresholdChannel[] channels,
        Integer escalationMinutes,
        boolean active,
        UUID updatedBy,
        OffsetDateTime updatedAt
) {
}
