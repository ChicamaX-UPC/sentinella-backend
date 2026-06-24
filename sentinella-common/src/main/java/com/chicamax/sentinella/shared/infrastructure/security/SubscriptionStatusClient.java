package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;

public interface SubscriptionStatusClient {

    boolean hasActiveSubscription(Jwt jwt);

    SubscriptionQuota quotaFor(Jwt jwt);

    record SubscriptionQuota(boolean active, int sensorLimit, int currentNodes) {
    }
}
