package com.chicamax.sentinella.subscriptions.application.internal.commandservices;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.BillingDunningRetryMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.BillingRenewalReminderMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.BillingServiceSuspendedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.PaymentCompletedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionPaymentFailedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SubscriptionPeriodSyncMessage;
import com.chicamax.sentinella.subscriptions.application.internal.PlanMetadataResolver;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.SubscriptionHistoryEntry;
import com.chicamax.sentinella.subscriptions.domain.model.events.SubscriptionActivatedEvent;
import com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import com.chicamax.sentinella.subscriptions.infrastructure.messaging.ProfilePlanClearedRabbitPublisher;
import com.chicamax.sentinella.subscriptions.infrastructure.messaging.SubscriptionActivatedRabbitPublisher;
import com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa.SubscriptionHistoryRepository;
import com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCommandServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PlanMetadataResolver planMetadataResolver;
    private final SubscriptionActivatedRabbitPublisher subscriptionActivatedRabbitPublisher;
    private final ProfilePlanClearedRabbitPublisher profilePlanClearedRabbitPublisher;
    private final RabbitTemplate rabbitTemplate;

    public SubscriptionCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            SubscriptionHistoryRepository subscriptionHistoryRepository,
            PlanMetadataResolver planMetadataResolver,
            SubscriptionActivatedRabbitPublisher subscriptionActivatedRabbitPublisher,
            ProfilePlanClearedRabbitPublisher profilePlanClearedRabbitPublisher,
            RabbitTemplate rabbitTemplate
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionHistoryRepository = subscriptionHistoryRepository;
        this.planMetadataResolver = planMetadataResolver;
        this.subscriptionActivatedRabbitPublisher = subscriptionActivatedRabbitPublisher;
        this.profilePlanClearedRabbitPublisher = profilePlanClearedRabbitPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public Subscription activateFromPayment(PaymentCompletedMessage message) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        subscriptionRepository.findBillableByUserId(message.userId()).stream()
                .filter(subscription -> subscription.isAccessibleAt(now))
                .forEach(existing -> {
                    existing.cancel(now);
                    subscriptionRepository.save(existing);
                });

        var meta = planMetadataResolver.resolve(message.planId());
        Subscription subscription = Subscription.activate(
                UUID.randomUUID(),
                message.userId(),
                message.planId(),
                meta.planType(),
                meta.sensorLimit(),
                message.stripeSubscriptionId()
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
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return subscriptionRepository.findBillableByUserId(userId).stream()
                .filter(subscription -> subscription.isAccessibleAt(now))
                .findFirst();
    }

    @Override
    @Transactional
    public void cancelByStripeSubscriptionId(String stripeSubscriptionId) {
        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            return;
        }
        subscriptionRepository.findFirstByStripeSubscriptionId(stripeSubscriptionId).ifPresent(subscription -> {
            subscription.cancel(OffsetDateTime.now());
            subscriptionRepository.save(subscription);
            subscriptionHistoryRepository.save(SubscriptionHistoryEntry.cancelled(
                    subscription.getId(),
                    "{\"stripeSubscriptionId\":\"" + stripeSubscriptionId + "\"}"
            ));
            profilePlanClearedRabbitPublisher.publish(subscription.getUserId(), subscription.getId(), stripeSubscriptionId);
        });
    }

    @Override
    @Transactional
    public void syncBillingPeriod(SubscriptionPeriodSyncMessage message) {
        if (message.stripeSubscriptionId() == null || message.stripeSubscriptionId().isBlank()) {
            return;
        }
        subscriptionRepository.findFirstByStripeSubscriptionId(message.stripeSubscriptionId()).ifPresent(subscription -> {
            subscription.syncBillingPeriod(message.currentPeriodEnd(), message.billingEmail());
            if (message.paymentRecovered()) {
                subscription.markPaymentRecovered();
            }
            subscriptionRepository.save(subscription);
        });
    }

    @Override
    @Transactional
    public void recordPaymentFailed(SubscriptionPaymentFailedMessage message) {
        if (message.stripeSubscriptionId() == null || message.stripeSubscriptionId().isBlank()) {
            return;
        }
        subscriptionRepository.findFirstByStripeSubscriptionId(message.stripeSubscriptionId()).ifPresent(subscription -> {
            if (message.billingEmail() != null && !message.billingEmail().isBlank()) {
                subscription.syncBillingPeriod(null, message.billingEmail());
            }
            subscription.markPaymentFailed(
                    message.stripeInvoiceId(),
                    message.attemptedAt() != null ? message.attemptedAt() : OffsetDateTime.now(ZoneOffset.UTC)
            );
            subscriptionRepository.save(subscription);
        });
    }

    @Override
    @Transactional
    public void runBillingLifecycle() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        sendRenewalWarnings(now);
        retryFailedPayments(now);
        suspendExpiredPastDue(now);
    }

    private void sendRenewalWarnings(OffsetDateTime now) {
        List<Subscription> due = subscriptionRepository.findNeedingRenewalWarning(now, now.plusDays(7));
        for (Subscription subscription : due) {
            String email = subscription.getBillingEmail();
            if (email == null || email.isBlank()) {
                continue;
            }
            rabbitTemplate.convertAndSend(
                    SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                    SentinellaMessagingConstants.BILLING_RENEWAL_REMINDER_ROUTING,
                    new BillingRenewalReminderMessage(email, null, subscription.getCurrentPeriodEnd())
            );
            subscription.markRenewalWarningSent(now);
            subscriptionRepository.save(subscription);
            log.info("Aviso de renovación programado para suscripción {}", subscription.getId());
        }
    }

    private void retryFailedPayments(OffsetDateTime now) {
        List<Subscription> due = subscriptionRepository.findNeedingDunningRetry(now, now.minusDays(2));
        for (Subscription subscription : due) {
            rabbitTemplate.convertAndSend(
                    SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                    SentinellaMessagingConstants.BILLING_DUNNING_RETRY_ROUTING,
                    new BillingDunningRetryMessage(subscription.getLastInvoiceId())
            );
            subscription.recordDunningRetry(now);
            subscriptionRepository.save(subscription);
            log.info("Reintento de cobro para suscripción {}", subscription.getId());
        }
    }

    private void suspendExpiredPastDue(OffsetDateTime now) {
        List<Subscription> due = subscriptionRepository.findPastDueReadyToSuspend(now);
        for (Subscription subscription : due) {
            subscription.suspendForNonPayment(now);
            subscriptionRepository.save(subscription);
            subscriptionHistoryRepository.save(SubscriptionHistoryEntry.cancelled(
                    subscription.getId(),
                    "{\"reason\":\"billing_cutoff\",\"stripeSubscriptionId\":\"" + subscription.getStripeSubscriptionId() + "\"}"
            ));
            profilePlanClearedRabbitPublisher.publish(
                    subscription.getUserId(),
                    subscription.getId(),
                    subscription.getStripeSubscriptionId()
            );
            String email = subscription.getBillingEmail();
            if (email != null && !email.isBlank()) {
                rabbitTemplate.convertAndSend(
                        SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                        SentinellaMessagingConstants.BILLING_SERVICE_SUSPENDED_ROUTING,
                        new BillingServiceSuspendedMessage(email, null)
                );
            }
            log.warn("Suscripción {} suspendida por falta de pago", subscription.getId());
        }
    }
}
