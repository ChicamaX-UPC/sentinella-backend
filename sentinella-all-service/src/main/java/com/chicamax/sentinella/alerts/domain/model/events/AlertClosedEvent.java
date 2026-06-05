package com.chicamax.sentinella.alerts.domain.model.events;

import java.util.UUID;

public record AlertClosedEvent(UUID alertId, UUID nodeId) {
}
