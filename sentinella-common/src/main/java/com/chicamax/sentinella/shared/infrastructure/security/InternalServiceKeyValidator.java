package com.chicamax.sentinella.shared.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InternalServiceKeyValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InternalServiceKeyValidator.class);

    private final String serviceKey;
    private final boolean requireKey;

    public InternalServiceKeyValidator(
            @Value("${sentinella.internal.service-key:}") String serviceKey,
            @Value("${sentinella.internal.require-key:false}") boolean requireKey
    ) {
        this.serviceKey = serviceKey;
        this.requireKey = requireKey;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (requireKey && (serviceKey == null || serviceKey.isBlank())) {
            throw new IllegalStateException(
                    "sentinella.internal.service-key es obligatorio (sentinella.internal.require-key=true)"
            );
        }
        if (!requireKey && (serviceKey == null || serviceKey.isBlank())) {
            log.warn("sentinella.internal.service-key vacía: endpoints /v1/internal/** sin protección");
        }
    }
}
