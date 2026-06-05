package com.chicamax.sentinella.alerts.infrastructure.persistence.jpa;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertAuditEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertAuditEntryRepository extends JpaRepository<AlertAuditEntry, UUID> {
    List<AlertAuditEntry> findByAlertIdOrderByTimestampDesc(UUID alertId);

    boolean existsByAlertIdAndAction(java.util.UUID alertId, String action);
}
