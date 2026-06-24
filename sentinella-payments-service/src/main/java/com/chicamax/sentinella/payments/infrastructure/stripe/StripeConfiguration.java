package com.chicamax.sentinella.payments.infrastructure.stripe;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfiguration {

    private final StripeProperties stripeProperties;

    public StripeConfiguration(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    @PostConstruct
    void init() {
        if (stripeProperties.isConfigured()) {
            Stripe.apiKey = stripeProperties.secretKey();
        }
    }
}
