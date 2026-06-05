package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ThresholdExceededMessage(
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        BigDecimal value,
        String severity
) {
}
