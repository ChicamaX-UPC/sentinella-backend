package com.chicamax.sentinella.subscriptions.application.internal.commandservices;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.PaymentCompletedMessage;
import com.chicamax.sentinella.subscriptions.application.internal.PlanMetadataResolver;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.SubscriptionHistoryEntry;
import com.chicamax.sentinella.subscriptions.domain.model.events.SubscriptionActivatedEvent;
import com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import com.chicamax.sentinella.subscriptions.infrastructure.messaging.SubscriptionActivatedRabbitPublisher;
import com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa.SubscriptionHistoryRepository;
import com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa.SubscriptionRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PlanMetadataResolver planMetadataResolver;
    private final SubscriptionActivatedRabbitPublisher subscriptionActivatedRabbitPublisher;

    public SubscriptionCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            SubscriptionHistoryRepository subscriptionHistoryRepository,
            PlanMetadataResolver planMetadataResolver,
            SubscriptionActivatedRabbitPublisher subscriptionActivatedRabbitPublisher
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionHistoryRepository = subscriptionHistoryRepository;
        this.planMetadataResolver = planMetadataResolver;
        this.subscriptionActivatedRabbitPublisher = subscriptionActivatedRabbitPublisher;
    }

    @Override
    @Transactional
    public Subscription activateFromPayment(PaymentCompletedMessage message) {
        var meta = planMetadataResolver.resolve(message.planId());
        Subscription subscription = Subscription.activate(
                UUID.randomUUID(),
                message.userId(),
                message.planId(),
                meta.planType(),
                meta.sensorLimit()
        );
        Subscription saved = subscriptionRepository.save(subscription);
        subscriptionHistoryRepository.save(SubscriptionHistoryEntry.activated(
                saved.getId(),
                "{\"paymentId\":\"" + message.paymentId() + "\"}"
        ));
        subscriptionActivatedRabbitPublisher.publish(new SubscriptionActivatedEvent(
                saved.getUserId(),
                saved.getId(),
                saved.getPlanType(),
                saved.getSensorLimit()
        ));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findActiveByUserId(UUID userId) {
        return subscriptionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, SubscriptionStatus.ACTIVE);
    }
}
