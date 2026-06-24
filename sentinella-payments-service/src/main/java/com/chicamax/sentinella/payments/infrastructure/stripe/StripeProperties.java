package com.chicamax.sentinella.payments.infrastructure.stripe;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(
        String secretKey,
        String webhookSecret,
        String successUrl,
        String cancelUrl,
        String portalReturnUrl,
        Map<String, StripePlanPriceIds> plans
) {
    public StripeProperties {
        secretKey = secretKey == null ? "" : secretKey;
        webhookSecret = webhookSecret == null ? "" : webhookSecret;
        successUrl = successUrl == null ? "" : successUrl;
        cancelUrl = cancelUrl == null ? "" : cancelUrl;
        portalReturnUrl = portalReturnUrl == null ? "" : portalReturnUrl;
    }

    public record StripePlanPriceIds(String recurringPriceId, String setupPriceId) {
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank();
    }

    public StripePlanPriceIds resolvePlanPrices(String planCode) {
        if (plans == null || planCode == null) {
            return null;
        }
        return plans.get(planCode.toLowerCase());
    }
}
