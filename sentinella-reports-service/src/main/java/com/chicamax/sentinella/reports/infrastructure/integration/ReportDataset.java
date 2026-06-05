package com.chicamax.sentinella.reports.infrastructure.integration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReportDataset(
        UUID tailingDamId,
        String damLabel,
        OffsetDateTime from,
        OffsetDateTime to,
        List<NodeLine> nodes,
        List<ReadingStatsLine> readingStats,
        List<AlertLine> alerts,
        List<RoundLine> rounds
) {

    public record NodeLine(
            UUID id,
            String externalId,
            String name,
            String sensorType,
            String status
    ) {
    }

    public record ReadingStatsLine(
            String nodeLabel,
            String sensorType,
            long count,
            BigDecimal min,
            BigDecimal max,
            BigDecimal avg,
            String unit,
            String lastStatus,
            OffsetDateTime lastAt
    ) {
    }

    public record AlertLine(
            UUID id,
            UUID nodeId,
            String sensorType,
            BigDecimal value,
            String severity,
            String status
    ) {
    }

    public record RoundLine(
            UUID id,
            OffsetDateTime scheduledAt,
            OffsetDateTime completedAt,
            String status,
            UUID operatorId
    ) {
    }
}
