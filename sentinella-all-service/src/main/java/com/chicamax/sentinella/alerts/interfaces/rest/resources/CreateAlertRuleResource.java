package com.chicamax.sentinella.alerts.interfaces.rest.resources;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateAlertRuleResource(
        @NotNull UUID nodeId,
        @NotBlank String sensorType,
        @NotNull AlertRuleOperator operator,
        @NotNull BigDecimal thresholdValue,
        @NotNull AlertSeverity severity,
        @NotNull AlertChannel[] channels,
        Integer escalationMinutes
) {
}
