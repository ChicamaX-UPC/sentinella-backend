package com.chicamax.sentinella.fieldoperations.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.entities.ChecklistItem;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainHash;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainRegisterMessage;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoundBlockchainPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RoundBlockchainPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSynced(InspectionRound round, List<ChecklistItem> items) {
        String checklistDigest = items.stream()
                .sorted(Comparator.comparing(ChecklistItem::getId))
                .map(item -> item.getId() + ":" + (item.getCompletedAt() == null ? "PENDING" : item.getCompletedAt()))
                .collect(Collectors.joining(";"));
        String canonical = String.join("|",
                "ROUND_SYNC",
                round.getId().toString(),
                round.getOperatorId().toString(),
                round.getTailingDamId().toString(),
                round.getSyncedAt() == null ? "" : round.getSyncedAt().toString(),
                checklistDigest
        );
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                BlockchainRegisterMessage.of(
                        UUID.randomUUID(),
                        "ROUND_SYNC",
                        round.getId(),
                        round.getTailingDamId(),
                        BlockchainHash.sha256(canonical)
                )
        );
    }
}
