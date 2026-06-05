package com.chicamax.sentinella.payments.interfaces.rest.resources;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record CheckoutResource(@NotNull UUID planId) {
}
