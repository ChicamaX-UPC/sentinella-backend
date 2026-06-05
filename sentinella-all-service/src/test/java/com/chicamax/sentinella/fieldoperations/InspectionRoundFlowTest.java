package com.chicamax.sentinella.fieldoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.valueobjects.RoundStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InspectionRoundFlowTest {

    @Test
    void shouldMarkRoundSyncedAfterCompletion() {
        InspectionRound round = new InspectionRound(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.now(),
                true
        );
        round.start();
        round.complete();
        round.markSynced();

        assertEquals(RoundStatus.SYNCED, round.getStatus());
    }
}
