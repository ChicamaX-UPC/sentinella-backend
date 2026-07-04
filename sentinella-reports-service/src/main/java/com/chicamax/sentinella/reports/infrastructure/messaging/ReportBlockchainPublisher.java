package com.chicamax.sentinella.reports.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainHash;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainRegisterMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** US17 — ancla hash canonico de informes regulatorios OEFA en blockchain.register. */
@Component
public class ReportBlockchainPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ReportBlockchainPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishRegulatoryReport(Report report, byte[] pdfContent) {
        if (report.getType() != ReportType.REGULATORY_OEFA) {
            return;
        }
        String pdfSha256 = BlockchainHash.sha256(pdfContent);
        String canonical = canonicalRegulatoryReport(report, pdfSha256);
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                BlockchainRegisterMessage.of(
                        UUID.randomUUID(),
                        "REGULATORY_REPORT",
                        report.getId(),
                        report.getTailingDamId(),
                        BlockchainHash.sha256(canonical),
                        report.getId()
                )
        );
    }

    static String canonicalRegulatoryReport(Report report, String pdfSha256) {
        return String.join("|",
                "REGULATORY_REPORT",
                report.getId().toString(),
                report.getTailingDamId() == null ? "" : report.getTailingDamId().toString(),
                report.getFromDate() == null ? "" : report.getFromDate().toString(),
                report.getToDate() == null ? "" : report.getToDate().toString(),
                report.getGeneratedBy().toString(),
                report.getStorageKey(),
                report.getFormat().name(),
                pdfSha256
        );
    }
}
