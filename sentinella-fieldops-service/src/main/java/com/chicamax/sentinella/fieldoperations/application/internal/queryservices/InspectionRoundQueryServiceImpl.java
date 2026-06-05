package com.chicamax.sentinella.fieldoperations.application.internal.queryservices;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.entities.ChecklistItem;
import com.chicamax.sentinella.fieldoperations.domain.model.queries.GetRoundsByOperatorQuery;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundQueryService;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.ChecklistItemRepository;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.InspectionRoundRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class InspectionRoundQueryServiceImpl implements InspectionRoundQueryService {

    private final InspectionRoundRepository inspectionRoundRepository;
    private final ChecklistItemRepository checklistItemRepository;

    public InspectionRoundQueryServiceImpl(
            InspectionRoundRepository inspectionRoundRepository,
            ChecklistItemRepository checklistItemRepository
    ) {
        this.inspectionRoundRepository = inspectionRoundRepository;
        this.checklistItemRepository = checklistItemRepository;
    }

    @Override
    public Page<InspectionRound> handle(GetRoundsByOperatorQuery query) {
        var pageable = PageRequest.of(query.safePage(), query.safeSize());
        return inspectionRoundRepository.findByOperatorIdOrderByScheduledAtDesc(query.operatorId(), pageable);
    }

    @Override
    public Optional<InspectionRound> findById(UUID roundId) {
        return inspectionRoundRepository.findById(roundId);
    }

    @Override
    public List<ChecklistItem> findChecklistItemsByRoundId(UUID roundId) {
        return checklistItemRepository.findByRoundId(roundId);
    }
}
