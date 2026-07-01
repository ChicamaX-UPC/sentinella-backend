package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Fallback permisivo de {@link SubscriptionStatusClient}. Se registra como bean vía
 * {@link SubscriptionStatusClientAutoConfiguration} (con {@code @ConditionalOnMissingBean}),
 * de modo que cualquier servicio que no aporte su propia implementación obtiene este No-Op.
 */
public class NoOpSubscriptionStatusClient implements SubscriptionStatusClient {

    @Override
    public boolean hasActiveSubscription(Jwt jwt) {
        return true;
    }

    @Override
    public SubscriptionQuota quotaFor(Jwt jwt) {
        return new SubscriptionQuota(true, Integer.MAX_VALUE, 0);
    }
}
