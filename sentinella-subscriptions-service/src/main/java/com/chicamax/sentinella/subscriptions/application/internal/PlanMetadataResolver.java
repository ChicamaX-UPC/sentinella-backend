package com.chicamax.sentinella.subscriptions.application.internal;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PlanMetadataResolver {

    public record PlanMetadata(String planType, int sensorLimit) {
    }

    public PlanMetadata resolve(UUID planId) {
        if (UUID.fromString("11111111-1111-1111-1111-111111111201").equals(planId)) {
            return new PlanMetadata("ECONOMY", 5);
        }
        if (UUID.fromString("11111111-1111-1111-1111-111111111202").equals(planId)) {
            return new PlanMetadata("PREMIUM", 12);
        }
        if (UUID.fromString("11111111-1111-1111-1111-111111111203").equals(planId)) {
            return new PlanMetadata("MAX", 20);
        }
        if (UUID.fromString("11111111-1111-1111-1111-111111111101").equals(planId)) {
            return new PlanMetadata("STARTER", 10);
        }
        if (UUID.fromString("11111111-1111-1111-1111-111111111102").equals(planId)) {
            return new PlanMetadata("PRO", 50);
        }
        return new PlanMetadata("CUSTOM", 5);
    }
}
