package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Valida suscripción activa vía {@link SubscriptionStatusClient} (implementado por servicio).
 */
@Component
public class SubscriptionGuard {

    private final SubscriptionStatusClient subscriptionStatusClient;
    private final boolean enforcementEnabled;

    public SubscriptionGuard(
            SubscriptionStatusClient subscriptionStatusClient,
            @org.springframework.beans.factory.annotation.Value("${sentinella.subscription.enforcement-enabled:true}")
            boolean enforcementEnabled
    ) {
        this.subscriptionStatusClient = subscriptionStatusClient;
        this.enforcementEnabled = enforcementEnabled;
    }

    public void requireActiveSubscription(Jwt jwt) {
        if (!enforcementEnabled || jwt == null) {
            return;
        }
        if (!subscriptionStatusClient.hasActiveSubscription(jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Suscripción activa requerida para esta operación"
            );
        }
    }

    public int requireSensorCapacity(Jwt jwt, int additionalNodes) {
        if (!enforcementEnabled || jwt == null) {
            return Integer.MAX_VALUE;
        }
        SubscriptionStatusClient.SubscriptionQuota quota = subscriptionStatusClient.quotaFor(jwt);
        if (!quota.active()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Suscripción activa requerida");
        }
        if (quota.currentNodes() + additionalNodes > quota.sensorLimit()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Límite de sensores del plan alcanzado (" + quota.sensorLimit() + ")"
            );
        }
        return quota.sensorLimit();
    }
}
