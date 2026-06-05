package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record SensorRegisteredMessage(UUID sensorId, UUID nodeId, UUID ownerUserId, String sensorType) {
}
