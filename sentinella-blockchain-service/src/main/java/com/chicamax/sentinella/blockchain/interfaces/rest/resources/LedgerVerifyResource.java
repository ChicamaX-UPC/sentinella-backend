package com.chicamax.sentinella.blockchain.interfaces.rest.resources;

import java.util.UUID;

public record LedgerVerifyResource(
        UUID recordId,
        String entityType,
        UUID entityId,
        String contentHash,
        String fabricTxId,
        boolean indexed,
        boolean onChain,
        boolean verified
) {
}
