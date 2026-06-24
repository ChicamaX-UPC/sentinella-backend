package com.chicamax.sentinella.payments.domain.model.commands;

import java.util.UUID;

public record CreateCheckoutCommand(UUID userId, UUID planId, String customerEmail) {
}
