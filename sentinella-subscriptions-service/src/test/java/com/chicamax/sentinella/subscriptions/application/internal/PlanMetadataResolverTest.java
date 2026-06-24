package com.chicamax.sentinella.subscriptions.application.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanMetadataResolverTest {

    private final PlanMetadataResolver resolver = new PlanMetadataResolver();

    @Test
    void resolvesLandingPlans() {
        assertThat(resolver.resolve(UUID.fromString("11111111-1111-1111-1111-111111111201")).planType())
                .isEqualTo("ECONOMY");
        assertThat(resolver.resolve(UUID.fromString("11111111-1111-1111-1111-111111111202")).sensorLimit())
                .isEqualTo(12);
        assertThat(resolver.resolve(UUID.fromString("11111111-1111-1111-1111-111111111203")).planType())
                .isEqualTo("MAX");
    }
}
