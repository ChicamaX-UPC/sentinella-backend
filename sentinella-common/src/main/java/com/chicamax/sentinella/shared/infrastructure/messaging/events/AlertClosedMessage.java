package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record AlertClosedMessage(UUID alertId, UUID nodeId) {
}
