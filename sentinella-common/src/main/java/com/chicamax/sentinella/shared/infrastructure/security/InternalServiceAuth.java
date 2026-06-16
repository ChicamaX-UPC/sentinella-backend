package com.chicamax.sentinella.shared.infrastructure.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class InternalServiceAuth {

    private InternalServiceAuth() {
    }

    public static void require(String configuredKey, String providedKey) {
        if (configuredKey == null || configuredKey.isBlank()) {
            return;
        }
        if (providedKey == null || !configuredKey.equals(providedKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clave de servicio interna invalida");
        }
    }
}
