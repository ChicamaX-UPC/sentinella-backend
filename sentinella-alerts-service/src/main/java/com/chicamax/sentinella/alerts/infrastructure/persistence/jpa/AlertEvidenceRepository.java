package com.chicamax.sentinella.alerts.infrastructure.persistence.jpa;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertEvidence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertEvidenceRepository extends JpaRepository<AlertEvidence, UUID> {
    List<AlertEvidence> findByAlertIdOrderByUploadedAtDesc(UUID alertId);
}
