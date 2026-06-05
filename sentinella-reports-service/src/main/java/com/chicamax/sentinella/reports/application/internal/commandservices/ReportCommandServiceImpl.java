package com.chicamax.sentinella.reports.application.internal.commandservices;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.domain.services.ReportCommandService;
import com.chicamax.sentinella.reports.domain.services.StorageService;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataCollector;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataset;
import com.chicamax.sentinella.reports.infrastructure.persistence.jpa.ReportRepository;
import com.chicamax.sentinella.reports.infrastructure.render.ReportFileRenderer;
import com.chicamax.sentinella.shared.infrastructure.mail.SendGridMailClient;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportCommandServiceImpl implements ReportCommandService {

    private final ReportRepository reportRepository;
    private final StorageService storageService;
    private final ReportFileRenderer reportFileRenderer;
    private final ReportDataCollector reportDataCollector;
    private final ObjectProvider<SendGridMailClient> sendGridMailClient;

    public ReportCommandServiceImpl(
            ReportRepository reportRepository,
            StorageService storageService,
            ReportFileRenderer reportFileRenderer,
            ReportDataCollector reportDataCollector,
            ObjectProvider<SendGridMailClient> sendGridMailClient
    ) {
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.reportFileRenderer = reportFileRenderer;
        this.reportDataCollector = reportDataCollector;
        this.sendGridMailClient = sendGridMailClient;
    }

    @Override
    @Transactional
    public Report generate(GenerateReportCommand command, String bearerToken) {
        ReportDataset dataset = reportDataCollector.collect(command, bearerToken);
        String filename = "report-%s.%s".formatted(UUID.randomUUID(), command.format().fileExtension());
        byte[] content = reportFileRenderer.render(command, dataset);
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
        Report saved = reportRepository.save(report);
        maybeSendByEmail(command.notifyEmail(), saved.getId(), command.type().name());
        return saved;
    }

    private void maybeSendByEmail(String email, UUID reportId, String reportType) {
        if (email == null || email.isBlank()) {
            return;
        }
        SendGridMailClient client = sendGridMailClient.getIfAvailable();
        if (client == null) {
            return;
        }
        client.send(
                email.trim(),
                "Sentinella — informe " + reportType,
                "Su informe " + reportId + " fue generado y esta disponible en la plataforma Sentinella."
        );
    }
}
