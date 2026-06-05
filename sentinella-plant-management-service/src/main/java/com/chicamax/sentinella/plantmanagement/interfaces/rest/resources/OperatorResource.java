package com.chicamax.sentinella.plantmanagement.interfaces.rest.resources;

import java.util.UUID;

public record OperatorResource(UUID id, UUID ownerId, String fullName, String email) {
}
