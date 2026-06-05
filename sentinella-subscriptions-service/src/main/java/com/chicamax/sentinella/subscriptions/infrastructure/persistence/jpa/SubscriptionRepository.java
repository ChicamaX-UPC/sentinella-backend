package com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa;

import com.chicamax.sentinella.subscriptions.domain.model.aggregates.Subscription;
import com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findFirstByUserIdAndStatusOrderByStartedAtDesc(UUID userId, SubscriptionStatus status);
}
