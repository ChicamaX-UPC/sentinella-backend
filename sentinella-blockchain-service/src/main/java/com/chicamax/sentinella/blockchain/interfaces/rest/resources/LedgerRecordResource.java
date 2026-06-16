package com.chicamax.sentinella.blockchain.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerRecordResource(
        UUID id,
        String entityType,
        UUID entityId,
        String contentHash,
        String fabricTxId,
        OffsetDateTime registeredAt
) {
}
