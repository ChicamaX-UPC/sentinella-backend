package com.chicamax.sentinella.payments.domain.model.aggregates;

import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "billing_customers", schema = "payments")
public class BillingCustomer extends AuditableAbstractAggregateRoot<BillingCustomer> {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "stripe_customer_id", nullable = false, unique = true)
    private String stripeCustomerId;

    @Column(nullable = false)
    private String email;

    protected BillingCustomer() {
    }

    public static BillingCustomer create(UUID userId, String stripeCustomerId, String email) {
        BillingCustomer customer = new BillingCustomer();
        customer.userId = userId;
        customer.stripeCustomerId = stripeCustomerId;
        customer.email = email;
        return customer;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public String getEmail() {
        return email;
    }
}
