package com.chicamax.sentinella.alerts.infrastructure.persistence.jpa;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertKind;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    @Query("""
            SELECT a FROM Alert a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:severity IS NULL OR a.severity = :severity)
              AND (:nodeId IS NULL OR a.nodeId = :nodeId)
              AND (:scoped = false OR a.nodeId IN :nodeIds)
            ORDER BY a.createdAt DESC
            """)
    Page<Alert> searchPaged(
            @Param("status") AlertStatus status,
            @Param("severity") AlertSeverity severity,
            @Param("nodeId") UUID nodeId,
            @Param("scoped") boolean scoped,
            @Param("nodeIds") Collection<UUID> nodeIds,
            Pageable pageable
    );

    Optional<Alert> findTopByRuleIdAndNodeIdAndSensorTypeAndStatusAndAlertKindOrderByCreatedAtDesc(
            UUID ruleId,
            UUID nodeId,
            String sensorType,
            AlertStatus status,
            AlertKind alertKind
    );

    List<Alert> findByStatus(AlertStatus status);
}
