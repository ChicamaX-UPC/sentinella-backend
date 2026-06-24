package com.chicamax.sentinella.subscriptions.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "subscription_history", schema = "subscriptions")
public class SubscriptionHistoryEntry {

    @Id
    private UUID id;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SubscriptionHistoryEntry() {
    }

    public static SubscriptionHistoryEntry activated(UUID subscriptionId, String payloadJson) {
        SubscriptionHistoryEntry entry = new SubscriptionHistoryEntry();
        entry.id = UUID.randomUUID();
        entry.subscriptionId = subscriptionId;
        entry.eventType = "ACTIVATED";
        entry.payload = payloadJson;
        entry.createdAt = OffsetDateTime.now();
        return entry;
    }

    public static SubscriptionHistoryEntry cancelled(UUID subscriptionId, String payloadJson) {
        SubscriptionHistoryEntry entry = new SubscriptionHistoryEntry();
        entry.id = UUID.randomUUID();
        entry.subscriptionId = subscriptionId;
        entry.eventType = "CANCELLED";
        entry.payload = payloadJson;
        entry.createdAt = OffsetDateTime.now();
        return entry;
    }
}
