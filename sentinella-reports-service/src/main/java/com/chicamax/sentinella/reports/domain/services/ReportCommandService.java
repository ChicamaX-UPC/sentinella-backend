package com.chicamax.sentinella.reports.domain.services;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;

public interface ReportCommandService {
    Report generate(GenerateReportCommand command, String bearerToken);
}
