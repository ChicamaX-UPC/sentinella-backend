package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdChannel;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateAlertRuleResource(
        @NotBlank String sensorType,
        @NotNull ThresholdRuleOperator operator,
        @NotNull BigDecimal thresholdValue,
        @NotNull ThresholdSeverity severity,
        @NotNull ThresholdChannel[] channels,
        Integer escalationMinutes,
        boolean active
) {
}
