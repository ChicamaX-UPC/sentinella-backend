package com.chicamax.sentinella.monitoring.domain.model.commands;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdChannel;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdSeverity;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateThresholdRuleCommand(
        UUID nodeId,
        String sensorType,
        ThresholdRuleOperator operator,
        BigDecimal thresholdValue,
        ThresholdSeverity severity,
        ThresholdChannel[] channels,
        Integer escalationMinutes,
        UUID updatedBy
) {
}
