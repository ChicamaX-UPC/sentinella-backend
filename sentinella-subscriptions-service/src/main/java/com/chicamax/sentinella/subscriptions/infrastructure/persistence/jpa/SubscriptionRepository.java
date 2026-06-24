package com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa;

import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findFirstByUserIdAndStatusOrderByStartedAtDesc(UUID userId, SubscriptionStatus status);

    Optional<Subscription> findFirstByStripeSubscriptionId(String stripeSubscriptionId);

    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status IN (com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus.ACTIVE,
                               com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus.PAST_DUE)
              AND s.currentPeriodEnd IS NOT NULL
              AND s.renewalWarningSentAt IS NULL
              AND s.currentPeriodEnd > :now
              AND s.currentPeriodEnd <= :warningUntil
            """)
    List<Subscription> findNeedingRenewalWarning(
            @Param("now") OffsetDateTime now,
            @Param("warningUntil") OffsetDateTime warningUntil
    );

    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status = com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus.PAST_DUE
              AND s.currentPeriodEnd IS NOT NULL
              AND s.currentPeriodEnd > :now
              AND s.lastInvoiceId IS NOT NULL
              AND (s.lastDunningAttemptAt IS NULL OR s.lastDunningAttemptAt <= :retryBefore)
            """)
    List<Subscription> findNeedingDunningRetry(
            @Param("now") OffsetDateTime now,
            @Param("retryBefore") OffsetDateTime retryBefore
    );

    @Query("""
            SELECT s FROM Subscription s
            WHERE s.status = com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus.PAST_DUE
              AND s.currentPeriodEnd IS NOT NULL
              AND s.currentPeriodEnd <= :now
            """)
    List<Subscription> findPastDueReadyToSuspend(@Param("now") OffsetDateTime now);

    @Query("""
            SELECT s FROM Subscription s
            WHERE s.userId = :userId
              AND s.status IN (com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus.ACTIVE,
                               com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus.PAST_DUE)
            ORDER BY s.startedAt DESC
            """)
    List<Subscription> findBillableByUserId(@Param("userId") UUID userId);
}
