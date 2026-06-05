package com.chicamax.sentinella.reports.infrastructure.persistence.jpa;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByTailingDamIdOrderByCreatedAtDesc(UUID tailingDamId);
}
