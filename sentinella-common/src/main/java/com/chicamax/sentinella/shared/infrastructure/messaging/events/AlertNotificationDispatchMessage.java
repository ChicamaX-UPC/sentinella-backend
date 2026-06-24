package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

/** Desacopla envío de notificaciones externas del hot path de creación de alertas. */
public record AlertNotificationDispatchMessage(UUID alertId, String channels) {
}
