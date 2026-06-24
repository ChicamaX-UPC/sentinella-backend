package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TelemetryPatternsResource(
        double waterRainCorrelation,
        BigDecimal avgRainMmPerHour,
        List<RateOfChangeResource> rateOfChangeByType,
        List<String> documentedPatterns
) {
    public record RateOfChangeResource(String sensorType, BigDecimal avgAbsDelta, BigDecimal maxAbsDelta) {
    }
}
