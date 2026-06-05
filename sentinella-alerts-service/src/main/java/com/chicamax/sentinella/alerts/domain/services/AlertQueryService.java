package com.chicamax.sentinella.alerts.domain.services;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertAuditEntry;
import com.chicamax.sentinella.alerts.domain.model.queries.GetActiveAlertsQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface AlertQueryService {
    Page<Alert> handle(GetActiveAlertsQuery query);

    Optional<Alert> findById(UUID alertId);

    List<AlertAuditEntry> getAuditLog(UUID alertId);
}
