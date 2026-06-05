package com.chicamax.sentinella.alerts.domain.model.commands;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateAlertRuleCommand(
        UUID nodeId,
        String sensorType,
        AlertRuleOperator operator,
        BigDecimal thresholdValue,
        AlertSeverity severity,
        AlertChannel[] channels,
        Integer escalationMinutes,
        UUID updatedBy
) {
}
