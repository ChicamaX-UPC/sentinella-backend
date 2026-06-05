package com.chicamax.sentinella.reports.domain.model.commands;

import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GenerateReportCommand(
        ReportType type,
        ReportFormat format,
        UUID tailingDamId,
        OffsetDateTime from,
        OffsetDateTime to,
        UUID generatedBy,
        String notifyEmail
) {
}
