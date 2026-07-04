package com.chicamax.sentinella.blockchain.infrastructure.messaging;

import com.chicamax.sentinella.blockchain.domain.model.aggregates.LedgerRecord;
import com.chicamax.sentinella.blockchain.domain.services.LedgerPort;
import com.chicamax.sentinella.blockchain.domain.services.LedgerRegistrationResult;
import com.chicamax.sentinella.blockchain.infrastructure.persistence.jpa.LedgerRecordRepository;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BlockchainRegisterConsumer {

    private static final Logger log = LoggerFactory.getLogger(BlockchainRegisterConsumer.class);

    private final LedgerRecordRepository ledgerRecordRepository;
    private final LedgerPort ledgerPort;

    public BlockchainRegisterConsumer(LedgerRecordRepository ledgerRecordRepository, LedgerPort ledgerPort) {
        this.ledgerRecordRepository = ledgerRecordRepository;
        this.ledgerPort = ledgerPort;
    }

    @RabbitListener(queues = "blockchain.register.queue")
    @Transactional
    public void onRegister(Map<String, Object> message) {
        UUID recordId = UUID.fromString(String.valueOf(message.get("recordId")));
        String entityType = String.valueOf(message.get("entityType"));
        UUID entityId = UUID.fromString(String.valueOf(message.get("entityId")));
        String contentHash = String.valueOf(message.get("contentHash"));
        UUID nodeId = parseOptionalUuid(message.get("nodeId"));
        UUID relatedEntityId = parseOptionalUuid(message.get("relatedEntityId"));
        if (relatedEntityId == null) {
            relatedEntityId = entityId;
        }

        try {
            LedgerRegistrationResult result = ledgerPort.register(recordId, entityType, entityId, nodeId, contentHash);
            ledgerRecordRepository.save(LedgerRecord.register(
                    recordId,
                    entityType,
                    entityId,
                    nodeId,
                    relatedEntityId,
                    contentHash,
                    result.fabricTxId(),
                    result.onChain()
            ));
        } catch (RuntimeException ex) {
            log.error(
                    "Fallo al anclar entityType={} entityId={} intento={}",
                    entityType,
                    entityId,
                    message.get("attempt"),
                    ex
            );
            throw ex;
        }
    }

    private static UUID parseOptionalUuid(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof UUID uuid) {
            return uuid;
        }
        if (raw instanceof Map<?, ?> map) {
            Object msb = map.get("mostSignificantBits");
            Object lsb = map.get("leastSignificantBits");
            if (msb instanceof Number most && lsb instanceof Number least) {
                return new UUID(most.longValue(), least.longValue());
            }
        }
        String value = String.valueOf(raw).trim();
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return UUID.fromString(value);
    }
}
