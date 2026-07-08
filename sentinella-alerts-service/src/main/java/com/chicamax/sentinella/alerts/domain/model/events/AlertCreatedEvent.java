package com.chicamax.sentinella.alerts.domain.model.events;

import java.math.BigDecimal;
import java.util.UUID;

public record AlertCreatedEvent(
        UUID alertId,
        UUID nodeId,
        String severity,
        String sensorType,
        BigDecimal triggeredValue
) {
}
