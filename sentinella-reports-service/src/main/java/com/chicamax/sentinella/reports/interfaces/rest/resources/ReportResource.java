package com.chicamax.sentinella.reports.interfaces.rest.resources;

import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportResource(
        UUID id,
        ReportType type,
        ReportFormat format,
        UUID tailingDamId,
        OffsetDateTime from,
        OffsetDateTime to,
        UUID generatedBy,
        String storageKey
) {
}
