package com.chicamax.sentinella.subscriptions.infrastructure.persistence.jpa;

import com.chicamax.sentinella.subscriptions.domain.model.aggregates.SubscriptionHistoryEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistoryEntry, UUID> {
}
