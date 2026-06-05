package com.chicamax.sentinella.iam.domain.model.commands;

import java.util.UUID;

public record UpdateProfileCommand(
        UUID userId,
        String fullName,
        String currentPassword,
        String newPassword
) {
}
