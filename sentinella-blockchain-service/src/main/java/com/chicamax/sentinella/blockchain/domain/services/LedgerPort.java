package com.chicamax.sentinella.blockchain.domain.services;

import java.util.UUID;

public interface LedgerPort {

    LedgerRegistrationResult register(
            UUID recordId,
            String entityType,
            UUID entityId,
            UUID nodeId,
            String contentHash
    );

    boolean verifyOnChain(String entityType, UUID entityId, String contentHash);
}
