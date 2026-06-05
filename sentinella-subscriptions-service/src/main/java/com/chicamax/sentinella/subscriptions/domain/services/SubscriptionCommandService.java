package com.chicamax.sentinella.subscriptions.domain.services;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.PaymentCompletedMessage;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionCommandService {
    Subscription activateFromPayment(PaymentCompletedMessage message);

    Optional<Subscription> findActiveByUserId(UUID userId);
}
