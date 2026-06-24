package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ForecastPointResource(OffsetDateTime timestamp, BigDecimal value, boolean projected) {
}
