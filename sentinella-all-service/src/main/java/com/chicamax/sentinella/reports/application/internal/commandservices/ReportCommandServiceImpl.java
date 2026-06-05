package com.chicamax.sentinella.reports.application.internal.commandservices;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.domain.services.ReportCommandService;
import com.chicamax.sentinella.reports.domain.services.StorageService;
import com.chicamax.sentinella.reports.infrastructure.persistence.jpa.ReportRepository;
import com.chicamax.sentinella.reports.infrastructure.render.ReportFileRenderer;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportCommandServiceImpl implements ReportCommandService {

    private final ReportRepository reportRepository;
    private final StorageService storageService;
    private final ReportFileRenderer reportFileRenderer;

    public ReportCommandServiceImpl(
            ReportRepository reportRepository,
            StorageService storageService,
            ReportFileRenderer reportFileRenderer
    ) {
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.reportFileRenderer = reportFileRenderer;
    }

    @Override
    @Transactional
    public Report generate(GenerateReportCommand command) {
        String filename = "report-%s.%s".formatted(UUID.randomUUID(), command.format().fileExtension());
        byte[] content = reportFileRenderer.render(command);
        String storageKey = storageService.saveReport(content, filename);

        Report report = new Report(
                UUID.randomUUID(),
                command.type(),
                command.format(),
                command.tailingDamId(),
                command.from(),
                command.to(),
                command.generatedBy(),
                storageKey
        );
        return reportRepository.save(report);
    }

}
