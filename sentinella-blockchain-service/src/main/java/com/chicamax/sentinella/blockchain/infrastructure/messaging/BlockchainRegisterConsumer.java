package com.chicamax.sentinella.blockchain.infrastructure.messaging;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa.LedgerRecordRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BlockchainRegisterConsumer {

    private final LedgerRecordRepository ledgerRecordRepository;

    public BlockchainRegisterConsumer(LedgerRecordRepository ledgerRecordRepository) {
        this.ledgerRecordRepository = ledgerRecordRepository;
    }

    @RabbitListener(queues = "blockchain.register.queue")
    @Transactional
    public void onRegister(Map<String, Object> message) {
        UUID recordId = UUID.fromString(String.valueOf(message.get("recordId")));
        String entityType = String.valueOf(message.get("entityType"));
        UUID entityId = UUID.fromString(String.valueOf(message.get("entityId")));
        String contentHash = String.valueOf(message.get("contentHash"));
        ledgerRecordRepository.save(LedgerRecord.register(recordId, entityType, entityId, contentHash));
    }
}
