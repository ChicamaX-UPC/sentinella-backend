package com.chicamax.sentinella.subscriptions.interfaces.rest.transform;

import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import com.chicamax.sentinella.subscriptions.interfaces.rest.resources.SubscriptionResource;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionResourceAssembler {

    public SubscriptionResource toResource(Subscription subscription) {
        return new SubscriptionResource(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlanId(),
                subscription.getPlanType(),
                subscription.getSensorLimit(),
                subscription.getStatus().name(),
                subscription.getStartedAt(),
                subscription.getExpiresAt()
        );
    }
}
