package com.chicamax.sentinella.reports.interfaces.rest.transform;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.interfaces.rest.resources.GenerateReportResource;
import com.chicamax.sentinella.reports.interfaces.rest.resources.ReportResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReportAssembler {

    public GenerateReportCommand toCommand(GenerateReportResource resource, UUID generatedBy) {
        return new GenerateReportCommand(
                resource.type(),
                resource.format(),
                resource.tailingDamId(),
                resource.from(),
                resource.to(),
                generatedBy,
                resource.notifyEmail()
        );
    }

    public ReportResource toResource(Report report) {
        return new ReportResource(
                report.getId(),
                report.getType(),
                report.getFormat(),
                report.getTailingDamId(),
                report.getFromDate(),
                report.getToDate(),
                report.getGeneratedBy(),
                report.getStorageKey()
        );
    }
}
