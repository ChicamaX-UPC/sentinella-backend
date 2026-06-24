package com.chicamax.sentinella.payments.domain.model.aggregates;

import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "plans", schema = "payments")
public class Plan extends AuditableAbstractAggregateRoot<Plan> {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(nullable = false)
    private String currency;

    @Column(name = "sensor_limit", nullable = false)
    private int sensorLimit;

    @Column(name = "billing_period", nullable = false)
    private String billingPeriod;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "stripe_product_id")
    private String stripeProductId;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @Column(name = "stripe_setup_price_id")
    private String stripeSetupPriceId;

    @Column(name = "setup_price_cents", nullable = false)
    private long setupPriceCents;

    protected Plan() {
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getPriceCents() {
        return priceCents;
    }

    public String getCurrency() {
        return currency;
    }

    public int getSensorLimit() {
        return sensorLimit;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public boolean isActive() {
        return active;
    }

    public String getStripeProductId() {
        return stripeProductId;
    }

    public String getStripePriceId() {
        return stripePriceId;
    }

    public String getStripeSetupPriceId() {
        return stripeSetupPriceId;
    }

    public long getSetupPriceCents() {
        return setupPriceCents;
    }
}
