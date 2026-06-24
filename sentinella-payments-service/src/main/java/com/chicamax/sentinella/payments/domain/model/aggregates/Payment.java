package com.chicamax.sentinella.payments.domain.model.aggregates;

import com.chicamax.sentinella.payments.domain.model.events.PaymentCompletedEvent;
import com.chicamax.sentinella.payments.domain.model.valueobjects.PaymentStatus;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payments")
public class Payment extends AuditableAbstractAggregateRoot<Payment> {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private String currency;

    protected Payment() {
    }

    public static Payment createPending(UUID id, UUID userId, UUID planId, long amountCents, String currency) {
        Payment payment = new Payment();
        payment.id = id;
        payment.userId = userId;
        payment.planId = planId;
        payment.status = PaymentStatus.PENDING;
        payment.amountCents = amountCents;
        payment.currency = currency;
        return payment;
    }

    public void attachStripeSessionId(String sessionId) {
        this.stripeSessionId = sessionId;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public void complete() {
        if (this.status == PaymentStatus.COMPLETED) {
            return;
        }
        this.status = PaymentStatus.COMPLETED;
        BigDecimal amount = BigDecimal.valueOf(amountCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        registerEvent(new PaymentCompletedEvent(id, userId, planId, amount, currency));
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

    public PaymentStatus getStatus() {
        return status;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }
}
