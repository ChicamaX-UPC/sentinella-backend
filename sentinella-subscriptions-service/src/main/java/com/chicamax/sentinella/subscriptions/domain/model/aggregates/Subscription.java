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

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "current_period_end")
    private OffsetDateTime currentPeriodEnd;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "renewal_warning_sent_at")
    private OffsetDateTime renewalWarningSentAt;

    @Column(name = "last_dunning_attempt_at")
    private OffsetDateTime lastDunningAttemptAt;

    @Column(name = "dunning_attempts", nullable = false)
    private int dunningAttempts;

    @Column(name = "last_invoice_id")
    private String lastInvoiceId;

    protected Subscription() {
    }

    public static Subscription activate(
            UUID id,
            UUID userId,
            UUID planId,
            String planType,
            int sensorLimit,
            String stripeSubscriptionId
    ) {
        Subscription subscription = new Subscription();
        subscription.id = id;
        subscription.userId = userId;
        subscription.planId = planId;
        subscription.planType = planType;
        subscription.sensorLimit = sensorLimit;
        subscription.status = SubscriptionStatus.ACTIVE;
        subscription.startedAt = OffsetDateTime.now();
        subscription.stripeSubscriptionId = stripeSubscriptionId;
        subscription.dunningAttempts = 0;
        subscription.registerEvent(new SubscriptionActivatedEvent(userId, id, planType, sensorLimit));
        return subscription;
    }

    public void cancel(OffsetDateTime expiresAt) {
        this.status = SubscriptionStatus.CANCELLED;
        this.expiresAt = expiresAt != null ? expiresAt : OffsetDateTime.now();
    }

    public void syncBillingPeriod(OffsetDateTime periodEnd, String email) {
        if (periodEnd != null) {
            this.currentPeriodEnd = periodEnd;
            if (this.renewalWarningSentAt != null && periodEnd.isAfter(this.renewalWarningSentAt.plusDays(1))) {
                this.renewalWarningSentAt = null;
            }
        }
        if (email != null && !email.isBlank()) {
            this.billingEmail = email.trim();
        }
    }

    public void markPaymentFailed(String invoiceId, OffsetDateTime attemptedAt) {
        this.status = SubscriptionStatus.PAST_DUE;
        this.lastInvoiceId = invoiceId;
        this.lastDunningAttemptAt = attemptedAt;
        this.dunningAttempts += 1;
    }

    public void markPaymentRecovered() {
        this.status = SubscriptionStatus.ACTIVE;
        this.lastInvoiceId = null;
        this.lastDunningAttemptAt = null;
        this.dunningAttempts = 0;
    }

    public void markRenewalWarningSent(OffsetDateTime at) {
        this.renewalWarningSentAt = at;
    }

    public void recordDunningRetry(OffsetDateTime at) {
        this.lastDunningAttemptAt = at;
        this.dunningAttempts += 1;
    }

    public void suspendForNonPayment(OffsetDateTime at) {
        this.status = SubscriptionStatus.EXPIRED;
        this.expiresAt = at;
    }

    public boolean isAccessibleAt(OffsetDateTime now) {
        if (this.status == SubscriptionStatus.ACTIVE) {
            return true;
        }
        if (this.status == SubscriptionStatus.PAST_DUE) {
            return this.currentPeriodEnd == null || this.currentPeriodEnd.isAfter(now);
        }
        return false;
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

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public OffsetDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public OffsetDateTime getRenewalWarningSentAt() {
        return renewalWarningSentAt;
    }

    public OffsetDateTime getLastDunningAttemptAt() {
        return lastDunningAttemptAt;
    }

    public int getDunningAttempts() {
        return dunningAttempts;
    }

    public String getLastInvoiceId() {
        return lastInvoiceId;
    }
}
