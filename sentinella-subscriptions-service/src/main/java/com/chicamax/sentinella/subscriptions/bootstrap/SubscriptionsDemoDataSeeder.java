package com.chicamax.sentinella.subscriptions.bootstrap;

import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import com.chicamax.sentinella.subscriptions.domain.model.aggregates.SubscriptionHistoryEntry;
import com.chicamax.sentinella.subscriptions.domain.model.events.SubscriptionActivatedEvent;
import com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.chicamax.sentinella.subscriptions.infrastructure.messaging.SubscriptionActivatedRabbitPublisher;
import com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa.SubscriptionHistoryRepository;
import com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Suscripción activa para {@code admin@sentinella.demo} (plan MAX hasta fin de 2028).
 * Idempotente: crea o extiende la suscripción demo existente.
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class SubscriptionsDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionsDemoDataSeeder.class);

    private static final OffsetDateTime DEMO_PERIOD_END =
            OffsetDateTime.of(2028, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

    private static final String DEMO_STRIPE_SUBSCRIPTION_ID = "demo_admin_subscription";

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final SubscriptionActivatedRabbitPublisher subscriptionActivatedRabbitPublisher;
    private final TransactionTemplate transactionTemplate;

    public SubscriptionsDemoDataSeeder(
            SubscriptionRepository subscriptionRepository,
            SubscriptionHistoryRepository subscriptionHistoryRepository,
            SubscriptionActivatedRabbitPublisher subscriptionActivatedRabbitPublisher,
            PlatformTransactionManager transactionManager
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionHistoryRepository = subscriptionHistoryRepository;
        this.subscriptionActivatedRabbitPublisher = subscriptionActivatedRabbitPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Optional<Subscription> existing = subscriptionRepository.findBillableByUserId(SentinellaDemoIds.USER_ADMIN)
                .stream()
                .filter(subscription -> subscription.isAccessibleAt(now))
                .findFirst();

        if (existing.isPresent()) {
            Subscription subscription = existing.get();
            if (subscription.getCurrentPeriodEnd() != null
                    && !subscription.getCurrentPeriodEnd().isBefore(DEMO_PERIOD_END)) {
                log.info("sentinella.seed (subscriptions): suscripción demo del admin ya vigente hasta 2028.");
                return;
            }
            subscription.syncBillingPeriod(DEMO_PERIOD_END, "admin@sentinella.demo");
            if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
                subscription.markPaymentRecovered();
            }
            subscriptionRepository.save(subscription);
            log.info("sentinella.seed (subscriptions): suscripción demo del admin extendida hasta 2028.");
            return;
        }

        Subscription subscription = Subscription.activate(
                SentinellaDemoIds.SUBSCRIPTION_ADMIN,
                SentinellaDemoIds.USER_ADMIN,
                SentinellaDemoIds.PLAN_MAX,
                "MAX",
                20,
                DEMO_STRIPE_SUBSCRIPTION_ID
        );
        subscription.syncBillingPeriod(DEMO_PERIOD_END, "admin@sentinella.demo");
        Subscription saved = subscriptionRepository.save(subscription);
        subscriptionHistoryRepository.save(SubscriptionHistoryEntry.activated(
                saved.getId(),
                "{\"source\":\"demo_seeder\"}"
        ));
        subscriptionActivatedRabbitPublisher.publish(new SubscriptionActivatedEvent(
                saved.getUserId(),
                saved.getId(),
                saved.getPlanType(),
                saved.getSensorLimit()
        ));
        log.info("sentinella.seed (subscriptions): suscripción MAX demo creada para admin hasta 2028.");
    }
}
