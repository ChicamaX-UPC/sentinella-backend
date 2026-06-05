package com.chicamax.sentinella.iam.interfaces.rest.resources;

import java.util.UUID;

public record UserSummaryResource(UUID id, String fullName, String email) {
}
