package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

public record AlertCreatedMessage(
        UUID alertId,
        UUID nodeId,
        String severity,
        String sensorType,
        BigDecimal triggeredValue
) {
}
