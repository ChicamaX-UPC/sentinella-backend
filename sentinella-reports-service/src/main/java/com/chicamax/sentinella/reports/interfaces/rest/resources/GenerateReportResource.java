package com.chicamax.sentinella.reports.interfaces.rest.resources;

import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GenerateReportResource(
        @NotNull ReportType type,
        @NotNull OffsetDateTime from,
        @NotNull OffsetDateTime to,
        @NotNull ReportFormat format,
        UUID tailingDamId,
        String notifyEmail
) {
}
