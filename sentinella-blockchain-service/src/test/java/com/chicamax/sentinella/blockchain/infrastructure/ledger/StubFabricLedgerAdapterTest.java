package com.chicamax.sentinella.blockchain.infrastructure.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.chicamax.sentinella.blockchain.domain.services.LedgerRegistrationResult;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StubFabricLedgerAdapterTest {

  private final StubFabricLedgerAdapter adapter = new StubFabricLedgerAdapter();

  @Test
  void register_returnsStubTxIdAndNotOnChain() {
    UUID recordId = UUID.randomUUID();
    LedgerRegistrationResult result = adapter.register(
        recordId,
        "ALERT",
        UUID.randomUUID(),
        UUID.randomUUID(),
        "abc123"
    );
    assertFalse(result.onChain());
    assertEquals("stub-" + recordId.toString().substring(0, 8), result.fabricTxId());
  }

  @Test
  void verifyOnChain_returnsFalseForStub() {
    assertFalse(adapter.verifyOnChain("ALERT", UUID.randomUUID(), "hash"));
  }
}
