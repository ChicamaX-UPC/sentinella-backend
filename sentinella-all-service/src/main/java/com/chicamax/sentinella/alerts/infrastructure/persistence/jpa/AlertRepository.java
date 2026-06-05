package com.chicamax.sentinella.alerts.infrastructure.persistence.jpa;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    @Query("""
            SELECT a FROM Alert a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:severity IS NULL OR a.severity = :severity)
              AND (:nodeId IS NULL OR a.nodeId = :nodeId)
            ORDER BY a.createdAt DESC
            """)
    List<Alert> search(
            @Param("status") AlertStatus status,
            @Param("severity") AlertSeverity severity,
            @Param("nodeId") UUID nodeId
    );

    Optional<Alert> findTopByRuleIdAndNodeIdAndSensorTypeAndStatusOrderByCreatedAtDesc(
            UUID ruleId,
            UUID nodeId,
            String sensorType,
            AlertStatus status
    );

    List<Alert> findByStatus(AlertStatus status);
}
