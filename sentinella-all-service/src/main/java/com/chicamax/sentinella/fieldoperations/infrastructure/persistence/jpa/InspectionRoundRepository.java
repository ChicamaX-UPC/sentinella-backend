package com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRoundRepository extends JpaRepository<InspectionRound, UUID> {
    List<InspectionRound> findByOperatorIdOrderByScheduledAtDesc(UUID operatorId);
}
