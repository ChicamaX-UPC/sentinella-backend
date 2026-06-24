package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReadingSnapshotResource(
        UUID id,
        UUID nodeId,
        String sensorType,
        OffsetDateTime bucketStart,
        BigDecimal avgValue,
        BigDecimal minValue,
        BigDecimal maxValue,
        int sampleCount
) {
}
