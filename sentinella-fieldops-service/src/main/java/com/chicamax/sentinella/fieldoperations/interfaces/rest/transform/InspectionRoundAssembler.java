package com.chicamax.sentinella.fieldoperations.interfaces.rest.transform;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CompleteChecklistItemCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CreateRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.entities.ChecklistItem;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.ChecklistItemResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.CompleteChecklistItemResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.CreateRoundResource;
import com.chicamax.sentinella.fieldoperations.interfaces.rest.resources.RoundResource;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InspectionRoundAssembler {

    public CreateRoundCommand toCommand(CreateRoundResource resource, UUID operatorId) {
        return new CreateRoundCommand(
                operatorId,
                resource.tailingDamId(),
                resource.scheduledAt(),
                resource.offlineCreated()
        );
    }

    public CompleteChecklistItemCommand toCommand(
            UUID roundId,
            UUID itemId,
            CompleteChecklistItemResource resource
    ) {
        return new CompleteChecklistItemCommand(
                roundId,
                itemId,
                resource.observations(),
                resource.photoS3Key(),
                resource.latitude(),
                resource.longitude(),
                resource.anomaly()
        );
    }

    public RoundResource toResource(InspectionRound round) {
        return toResource(round, List.of());
    }

    public RoundResource toResource(InspectionRound round, List<ChecklistItem> checklistItems) {
        return new RoundResource(
                round.getId(),
                round.getOperatorId(),
                round.getTailingDamId(),
                round.getScheduledAt(),
                round.getStartedAt(),
                round.getCompletedAt(),
                round.getStatus(),
                round.isOfflineCreated(),
                round.getSyncedAt(),
                checklistItems.stream().map(this::toChecklistItemResource).toList()
        );
    }

    private ChecklistItemResource toChecklistItemResource(ChecklistItem item) {
        return new ChecklistItemResource(
                item.getId(),
                item.getPointName(),
                item.isRequired(),
                item.getObservations(),
                item.getPhotoS3Key(),
                item.getLatitude(),
                item.getLongitude(),
                item.getCompletedAt(),
                item.isAnomaly()
        );
    }
}
