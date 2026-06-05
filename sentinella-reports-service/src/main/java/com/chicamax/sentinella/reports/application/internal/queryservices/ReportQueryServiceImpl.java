package com.chicamax.sentinella.reports.application.internal.queryservices;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.queries.GetReportsByDamQuery;
import com.chicamax.sentinella.reports.domain.services.ReportQueryService;
import com.chicamax.sentinella.reports.infrastructure.persistence.jpa.ReportRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ReportQueryServiceImpl implements ReportQueryService {

    private final ReportRepository reportRepository;

    public ReportQueryServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<Report> handle(GetReportsByDamQuery query) {
        if (query.tailingDamId() == null) {
            return reportRepository.findAll();
        }
        return reportRepository.findByTailingDamIdOrderByCreatedAtDesc(query.tailingDamId());
    }

    @Override
    public Optional<Report> findById(UUID reportId) {
        return reportRepository.findById(reportId);
    }
}
