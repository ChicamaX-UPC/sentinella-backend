package com.chicamax.sentinella.monitoring.domain.prediction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ForecastPoint(OffsetDateTime timestamp, BigDecimal value, boolean projected) {
}
