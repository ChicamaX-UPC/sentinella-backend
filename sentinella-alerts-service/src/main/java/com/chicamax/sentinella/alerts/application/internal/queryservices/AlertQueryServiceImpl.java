package com.chicamax.sentinella.alerts.application.internal.queryservices;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertAuditEntry;
import com.chicamax.sentinella.alerts.domain.model.queries.GetActiveAlertsQuery;
import com.chicamax.sentinella.alerts.domain.services.AlertQueryService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertAuditEntryRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;
    private final AlertAuditEntryRepository alertAuditEntryRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository, AlertAuditEntryRepository alertAuditEntryRepository) {
        this.alertRepository = alertRepository;
        this.alertAuditEntryRepository = alertAuditEntryRepository;
    }

    @Override
    public Page<Alert> handle(GetActiveAlertsQuery query) {
        var pageable = PageRequest.of(query.safePage(), query.safeSize());
        if (query.scoped() && (query.nodeIds() == null || query.nodeIds().isEmpty())) {
            return Page.empty(pageable);
        }
        return alertRepository.searchPaged(
                query.status(),
                query.severity(),
                query.nodeId(),
                query.scoped(),
                query.nodeIds(),
                pageable
        );
    }

    @Override
    public Optional<Alert> findById(UUID alertId) {
        return alertRepository.findById(alertId);
    }

    @Override
    public List<AlertAuditEntry> getAuditLog(UUID alertId) {
        return alertAuditEntryRepository.findByAlertIdOrderByTimestampDesc(alertId);
    }
}
