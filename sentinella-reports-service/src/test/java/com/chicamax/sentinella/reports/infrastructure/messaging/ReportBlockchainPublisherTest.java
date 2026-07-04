package com.chicamax.sentinella.reports.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainHash;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReportBlockchainPublisherTest {

    @Test
    void canonicalRegulatoryReportIncludesPdfHash() {
        UUID reportId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID damId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        OffsetDateTime from = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-01-31T23:59:59Z");
        Report report = new Report(
                reportId,
                ReportType.REGULATORY_OEFA,
                ReportFormat.PDF,
                damId,
                from,
                to,
                userId,
                "reports/oefa-demo.pdf"
        );
        String pdfSha256 = BlockchainHash.sha256(new byte[] {1, 2, 3});

        String canonical = ReportBlockchainPublisher.canonicalRegulatoryReport(report, pdfSha256);

        assertEquals(
                "REGULATORY_REPORT|11111111-1111-1111-1111-111111111111|22222222-2222-2222-2222-222222222222"
                        + "|" + from + "|" + to + "|33333333-3333-3333-3333-333333333333"
                        + "|reports/oefa-demo.pdf|PDF|" + pdfSha256,
                canonical
        );
    }
}
