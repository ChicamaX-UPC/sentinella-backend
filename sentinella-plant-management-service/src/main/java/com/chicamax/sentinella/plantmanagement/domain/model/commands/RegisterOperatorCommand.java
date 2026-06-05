package com.chicamax.sentinella.plantmanagement.domain.model.commands;

import java.util.UUID;

public record RegisterOperatorCommand(UUID ownerId, String fullName, String email) {
}
