package com.chicamax.sentinella.reports.infrastructure.messaging;

import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.reports.domain.services.ReportCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.ReportGenerateMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerationConsumer {

    private final ReportCommandService reportCommandService;

    public ReportGenerationConsumer(ReportCommandService reportCommandService) {
        this.reportCommandService = reportCommandService;
    }

    @RabbitListener(queues = "report.generate.queue")
    public void onGenerate(ReportGenerateMessage message) {
        reportCommandService.generate(
                new GenerateReportCommand(
                        ReportType.valueOf(message.type()),
                        ReportFormat.valueOf(message.format()),
                        message.tailingDamId(),
                        message.from(),
                        message.to(),
                        message.generatedBy(),
                        message.notifyEmail()
                ),
                message.bearerToken()
        );
    }
}
