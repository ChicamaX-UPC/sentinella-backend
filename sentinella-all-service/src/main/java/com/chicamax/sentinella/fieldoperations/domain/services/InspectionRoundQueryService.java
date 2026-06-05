package com.chicamax.sentinella.fieldoperations.domain.services;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.entities.ChecklistItem;
import com.chicamax.sentinella.fieldoperations.domain.model.queries.GetRoundsByOperatorQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InspectionRoundQueryService {
    List<InspectionRound> handle(GetRoundsByOperatorQuery query);

    Optional<InspectionRound> findById(UUID roundId);

    List<ChecklistItem> findChecklistItemsByRoundId(UUID roundId);
}
