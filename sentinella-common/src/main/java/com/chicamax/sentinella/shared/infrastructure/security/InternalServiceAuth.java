package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class InternalServiceAuth {

    private InternalServiceAuth() {
    }

    public static void require(String configuredKey, String providedKey, boolean requireKey) {
        if (configuredKey == null || configuredKey.isBlank()) {
            if (requireKey) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Clave de servicio interna no configurada"
                );
            }
            return;
        }
        if (providedKey == null || !configuredKey.equals(providedKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clave de servicio interna invalida");
        }
    }

    /** @deprecated use {@link #require(String, String, boolean)} */
    public static void require(String configuredKey, String providedKey) {
        require(configuredKey, providedKey, false);
    }
}
