package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Provee un {@link SubscriptionStatusClient} por defecto cuando el servicio no aporta el suyo.
 *
 * <p>Se modela como {@code @AutoConfiguration} (procesada después del component scan del servicio)
 * para que {@code @ConditionalOnMissingBean} sea fiable: si el servicio registra su propia
 * implementación (p. ej. {@code RestSubscriptionStatusClient} en plant-management) este bean se
 * inhibe; en caso contrario se usa {@link NoOpSubscriptionStatusClient}. Resuelve el fallo de
 * arranque de {@link SubscriptionGuard} en los servicios sin cliente propio.
 */
@AutoConfiguration
public class SubscriptionStatusClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SubscriptionStatusClient.class)
    public SubscriptionStatusClient noOpSubscriptionStatusClient() {
        return new NoOpSubscriptionStatusClient();
    }
}
