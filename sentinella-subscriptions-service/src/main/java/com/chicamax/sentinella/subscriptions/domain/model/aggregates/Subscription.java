package com.chicamax.sentinella.subscriptions.domain.model.aggregates;

import com.chicamax.sentinella.subscriptions.domain.model.events.SubscriptionActivatedEvent;
import com.chicamax.sentinella.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions", schema = "subscriptions")
public class Subscription extends AuditableAbstractAggregateRoot<Subscription> {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "plan_type", nullable = false)
    private String planType;

    @Column(name = "sensor_limit", nullable = false)
    private int sensorLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    protected Subscription() {
    }

    public static Subscription activate(
            UUID id,
            UUID userId,
            UUID planId,
            String planType,
            int sensorLimit
    ) {
        Subscription subscription = new Subscription();
        subscription.id = id;
        subscription.userId = userId;
        subscription.planId = planId;
        subscription.planType = planType;
        subscription.sensorLimit = sensorLimit;
        subscription.status = SubscriptionStatus.ACTIVE;
        subscription.startedAt = OffsetDateTime.now();
        subscription.registerEvent(new SubscriptionActivatedEvent(userId, id, planType, sensorLimit));
        return subscription;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getPlanType() {
        return planType;
    }

    public int getSensorLimit() {
        return sensorLimit;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}
