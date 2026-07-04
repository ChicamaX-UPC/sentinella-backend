package com.chicamax.sentinella.shared.infrastructure.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BlockchainRegisterMessageTest {

  @Test
  void of_includesRelatedEntityIdAndAttempt() {
    UUID recordId = UUID.randomUUID();
    UUID entityId = UUID.randomUUID();
    UUID nodeId = UUID.randomUUID();
    UUID alertId = UUID.randomUUID();

    Map<String, Object> message = BlockchainRegisterMessage.of(
        recordId,
        "ALERT_EVIDENCE",
        entityId,
        nodeId,
        "hash",
        alertId
    );

    assertEquals(recordId, message.get("recordId"));
    assertEquals("ALERT_EVIDENCE", message.get("entityType"));
    assertEquals(entityId, message.get("entityId"));
    assertEquals(alertId, message.get("relatedEntityId"));
    assertEquals(0, message.get("attempt"));
  }

  @Test
  void of_defaultsRelatedEntityToEntityId() {
    UUID entityId = UUID.randomUUID();
    Map<String, Object> message = BlockchainRegisterMessage.of(
        UUID.randomUUID(),
        "ALERT",
        entityId,
        UUID.randomUUID(),
        "hash"
    );
    assertTrue(message.containsKey("relatedEntityId"));
    assertEquals(entityId, message.get("relatedEntityId"));
  }
}
