package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Evento Monitoring → Alerts cuando se estima cruce de umbral antes de que ocurra. */
public record PredictiveBreachMessage(
        UUID ruleId,
        UUID nodeId,
        String sensorType,
        BigDecimal currentValue,
        String severity,
        String channels,
        OffsetDateTime estimatedBreachAt,
        Long leadTimeMinutes
) {
    public static PredictiveBreachMessage fromForecast(
            UUID ruleId,
            UUID nodeId,
            String sensorType,
            BigDecimal currentValue,
            String severity,
            String channels,
            OffsetDateTime estimatedBreachAt,
            Long leadTimeMinutes
    ) {
        return new PredictiveBreachMessage(
                ruleId,
                nodeId,
                sensorType,
                currentValue,
                severity,
                channels != null && !channels.isBlank() ? channels : "APP",
                estimatedBreachAt,
                leadTimeMinutes
        );
    }
}
