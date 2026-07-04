package com.chicamax.sentinella.blockchain.infrastructure.ledger;

import com.chicamax.sentinella.blockchain.domain.services.LedgerPort;
import com.chicamax.sentinella.blockchain.domain.services.LedgerRegistrationResult;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Ledger local (desarrollo / fallback cuando Fabric no esta habilitado). */
@Component
@ConditionalOnProperty(name = "sentinella.blockchain.fabric.enabled", havingValue = "false", matchIfMissing = true)
public class StubFabricLedgerAdapter implements LedgerPort {

    @Override
    public LedgerRegistrationResult register(
            UUID recordId,
            String entityType,
            UUID entityId,
            UUID nodeId,
            String contentHash
    ) {
        return new LedgerRegistrationResult("stub-" + recordId.toString().substring(0, 8), false);
    }

    @Override
    public boolean verifyOnChain(String entityType, UUID entityId, String contentHash) {
        return false;
    }
}
