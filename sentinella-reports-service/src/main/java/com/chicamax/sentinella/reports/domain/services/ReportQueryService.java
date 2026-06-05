package com.chicamax.sentinella.reports.domain.services;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.queries.GetReportsByDamQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportQueryService {
    List<Report> handle(GetReportsByDamQuery query);

    Optional<Report> findById(UUID reportId);
}
