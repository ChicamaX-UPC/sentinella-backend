package com.chicamax.sentinella.reports.application.internal.commandservices;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.domain.services.ReportCommandService;
import com.chicamax.sentinella.reports.domain.services.StorageService;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataCollector;
import com.chicamax.sentinella.reports.infrastructure.integration.ReportDataset;
import com.chicamax.sentinella.reports.infrastructure.persistence.jpa.ReportRepository;
import com.chicamax.sentinella.reports.infrastructure.messaging.ReportBlockchainPublisher;
import com.chicamax.sentinella.reports.infrastructure.render.ReportFileRenderer;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.mail.PlainTextMailClient;
import com.chicamax.sentinella.shared.infrastructure.mail.SentinellaEmailTemplate;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.ReportGenerateMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportCommandServiceImpl implements ReportCommandService {

    private final ReportRepository reportRepository;
    private final StorageService storageService;
    private final ReportFileRenderer reportFileRenderer;
    private final ReportDataCollector reportDataCollector;
    private final ObjectProvider<PlainTextMailClient> mailClient;
    private final RabbitTemplate rabbitTemplate;
    private final ReportBlockchainPublisher reportBlockchainPublisher;
    private final String appUrl;

    public ReportCommandServiceImpl(
            ReportRepository reportRepository,
            StorageService storageService,
            ReportFileRenderer reportFileRenderer,
            ReportDataCollector reportDataCollector,
            ObjectProvider<PlainTextMailClient> mailClient,
            RabbitTemplate rabbitTemplate,
            ReportBlockchainPublisher reportBlockchainPublisher,
            @Value("${sentinella.app-url:https://sentinella-frontend.vercel.app}") String appUrl
    ) {
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.reportFileRenderer = reportFileRenderer;
        this.reportDataCollector = reportDataCollector;
        this.mailClient = mailClient;
        this.rabbitTemplate = rabbitTemplate;
        this.reportBlockchainPublisher = reportBlockchainPublisher;
        this.appUrl = appUrl == null ? "https://sentinella-frontend.vercel.app" : appUrl.replaceAll("/$", "");
    }

    @Override
    public void enqueue(GenerateReportCommand command, String bearerToken) {
        UUID reportId = UUID.randomUUID();
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.REPORT_GENERATE_ROUTING,
                new ReportGenerateMessage(
                        reportId,
                        command.type().name(),
                        command.format().name(),
                        command.tailingDamId(),
                        command.from(),
                        command.to(),
                        command.generatedBy(),
                        command.notifyEmail(),
                        bearerToken
                )
        );
    }

    @Override
    @Transactional
    public Report generate(GenerateReportCommand command, String bearerToken) {
        ReportDataset dataset = reportDataCollector.collect(command, bearerToken);
        String filename = "report-%s.%s".formatted(UUID.randomUUID(), command.format().fileExtension());
        byte[] content = reportFileRenderer.render(command, dataset);
        String storageKey = storageService.saveReport(content, filename);

        UUID reportId = command.reportId() != null ? command.reportId() : UUID.randomUUID();
        Report report = new Report(
                reportId,
                command.type(),
                command.format(),
                command.tailingDamId(),
                command.from(),
                command.to(),
                command.generatedBy(),
                storageKey
        );
        Report saved = reportRepository.save(report);
        if (command.type() == ReportType.REGULATORY_OEFA) {
            reportBlockchainPublisher.publishRegulatoryReport(saved, content);
        }
        maybeSendByEmail(command.notifyEmail(), saved.getId(), command.type().name());
        return saved;
    }

    private void maybeSendByEmail(String email, UUID reportId, String reportType) {
        if (email == null || email.isBlank()) {
            return;
        }
        PlainTextMailClient client = mailClient.getIfAvailable();
        if (client == null) {
            return;
        }
        String text = "Su informe %s (%s) fue generado y está disponible en la plataforma Sentinella.".formatted(reportId, reportType);
        String htmlBody = SentinellaEmailTemplate.paragraphs(text)
                + SentinellaEmailTemplate.detailRow("Informe", reportType)
                + SentinellaEmailTemplate.detailRow("Referencia", reportId.toString());
        String html = SentinellaEmailTemplate.render(
                "Informe listo",
                htmlBody,
                "Ver informes",
                appUrl + "/reports"
        );
        client.sendRich(email.trim(), "Sentinella — informe " + reportType, text, html);
    }
}
