package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportGenerateMessage(
        UUID reportId,
        String type,
        String format,
        UUID tailingDamId,
        OffsetDateTime from,
        OffsetDateTime to,
        UUID generatedBy,
        String notifyEmail,
        String bearerToken
) {
}
