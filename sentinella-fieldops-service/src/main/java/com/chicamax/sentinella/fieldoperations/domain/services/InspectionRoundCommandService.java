package com.chicamax.sentinella.fieldoperations.domain.services;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CompleteChecklistItemCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CreateRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.SyncRoundCommand;

public interface InspectionRoundCommandService {
    InspectionRound createRound(CreateRoundCommand command);

    InspectionRound completeChecklistItem(CompleteChecklistItemCommand command);

    InspectionRound syncRound(SyncRoundCommand command);
}
