package com.chicamax.sentinella.payments.interfaces.rest.resources;

import java.util.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CheckoutResource(@NotNull UUID planId, @NotNull @Email String customerEmail) {
}
