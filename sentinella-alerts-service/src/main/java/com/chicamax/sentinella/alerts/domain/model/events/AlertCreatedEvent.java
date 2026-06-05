package com.chicamax.sentinella.alerts.domain.model.events;

import java.util.UUID;

public record AlertCreatedEvent(UUID alertId, UUID nodeId, String severity) {
}
