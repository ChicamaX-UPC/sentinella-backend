package com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa;

import com.chicamax.sentinella.fieldoperations.domain.model.entities.ChecklistItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, UUID> {
    List<ChecklistItem> findByRoundId(UUID roundId);

    java.util.Optional<ChecklistItem> findByIdAndRoundId(UUID id, UUID roundId);

    long countByRoundIdAndCompletedAtIsNull(UUID roundId);

    long countByRoundIdAndRequiredTrueAndCompletedAtIsNull(UUID roundId);
}
