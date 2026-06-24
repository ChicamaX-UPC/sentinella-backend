package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(SubscriptionStatusClient.class)
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
