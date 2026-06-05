package com.chicamax.sentinella.fieldoperations.application.internal.commandservices;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CompleteChecklistItemCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CreateRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.SyncRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.model.entities.ChecklistItem;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundCommandService;
import com.chicamax.sentinella.fieldoperations.infrastructure.messaging.RoundSyncedRabbitPublisher;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.ChecklistItemRepository;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.InspectionRoundRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.RoundSyncedRabbitMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class InspectionRoundCommandServiceImpl implements InspectionRoundCommandService {

    private final InspectionRoundRepository inspectionRoundRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final RoundSyncedRabbitPublisher roundSyncedRabbitPublisher;

    public InspectionRoundCommandServiceImpl(
            InspectionRoundRepository inspectionRoundRepository,
            ChecklistItemRepository checklistItemRepository,
            RoundSyncedRabbitPublisher roundSyncedRabbitPublisher
    ) {
        this.inspectionRoundRepository = inspectionRoundRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.roundSyncedRabbitPublisher = roundSyncedRabbitPublisher;
    }

    @Override
    @Transactional
    public InspectionRound createRound(CreateRoundCommand command) {
        InspectionRound round = new InspectionRound(
                UUID.randomUUID(),
                command.operatorId(),
                command.tailingDamId(),
                command.scheduledAt(),
                command.offlineCreated()
        );
        round.start();
        InspectionRound saved = inspectionRoundRepository.save(round);
        for (String point : List.of(
                "Coronamiento — tramo NE",
                "Drenes de pie y pozas",
                "Canal de coronacion (visual)"
        )) {
            checklistItemRepository.save(new ChecklistItem(UUID.randomUUID(), saved.getId(), point, true));
        }
        return saved;
    }

    @Override
    @Transactional
    public InspectionRound completeChecklistItem(CompleteChecklistItemCommand command) {
        InspectionRound round = inspectionRoundRepository.findById(command.roundId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        ChecklistItem item = checklistItemRepository.findById(command.itemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado"));
        if (!item.getRoundId().equals(round.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El item no pertenece a la ronda");
        }
        item.complete(
                command.observations(),
                command.photoS3Key(),
                command.latitude(),
                command.longitude(),
                command.anomaly()
        );
        checklistItemRepository.save(item);

        long pending = checklistItemRepository.countByRoundIdAndCompletedAtIsNull(round.getId());
        if (pending == 0) {
            round.complete();
            inspectionRoundRepository.save(round);
        }
        return round;
    }

    @Override
    @Transactional
    public InspectionRound syncRound(SyncRoundCommand command) {
        InspectionRound round = inspectionRoundRepository.findById(command.roundId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));
        round.markSynced();
        InspectionRound saved = inspectionRoundRepository.save(round);
        roundSyncedRabbitPublisher.publish(
                new RoundSyncedRabbitMessage(saved.getId(), saved.getOperatorId(), saved.getTailingDamId())
        );
        return saved;
    }
}
