package com.chicamax.sentinella.contracts.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SentinellaMessagingConstantsContractTest {

    @Test
    void shouldExposeStableEventContract() {
        assertEquals("sentinella", SentinellaMessagingConstants.SENTINELLA_EXCHANGE);
        assertEquals("threshold.exceeded", SentinellaMessagingConstants.THRESHOLD_EXCEEDED_ROUTING);
        assertEquals("round.synced", SentinellaMessagingConstants.ROUND_SYNCED_ROUTING);
    }

    @Test
    void shouldKeepRoutingKeysVersionableAndDotSeparated() {
        assertTrue(SentinellaMessagingConstants.ALERT_CREATED_ROUTING.contains("."));
        assertTrue(SentinellaMessagingConstants.NODE_OFFLINE_ROUTING.contains("."));
    }
}
