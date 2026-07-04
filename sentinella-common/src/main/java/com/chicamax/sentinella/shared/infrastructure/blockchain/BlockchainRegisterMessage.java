package com.chicamax.sentinella.shared.infrastructure.blockchain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Mensaje estándar para la cola blockchain.register (US17). */
public final class BlockchainRegisterMessage {

    private BlockchainRegisterMessage() {
    }

    public static Map<String, Object> of(
            UUID recordId,
            String entityType,
            UUID entityId,
            UUID nodeId,
            String contentHash
    ) {
        return of(recordId, entityType, entityId, nodeId, contentHash, entityId);
    }

    public static Map<String, Object> of(
            UUID recordId,
            String entityType,
            UUID entityId,
            UUID nodeId,
            String contentHash,
            UUID relatedEntityId
    ) {
        Map<String, Object> message = new HashMap<>();
        message.put("recordId", recordId);
        message.put("entityType", entityType);
        message.put("entityId", entityId);
        message.put("nodeId", nodeId);
        message.put("contentHash", contentHash);
        if (relatedEntityId != null) {
            message.put("relatedEntityId", relatedEntityId);
        }
        message.put("attempt", 0);
        return message;
    }
}
