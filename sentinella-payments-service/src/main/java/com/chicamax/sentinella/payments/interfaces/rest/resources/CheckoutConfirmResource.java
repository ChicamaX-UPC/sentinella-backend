package com.chicamax.sentinella.payments.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record CheckoutConfirmResource(@NotBlank String sessionId) {
}
