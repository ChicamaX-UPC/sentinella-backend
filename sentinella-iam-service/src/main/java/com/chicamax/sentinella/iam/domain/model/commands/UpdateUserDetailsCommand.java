package com.chicamax.sentinella.iam.domain.model.commands;

import java.util.UUID;

public record UpdateUserDetailsCommand(UUID userId, String fullName, String jobTitle, String phone) {
}
