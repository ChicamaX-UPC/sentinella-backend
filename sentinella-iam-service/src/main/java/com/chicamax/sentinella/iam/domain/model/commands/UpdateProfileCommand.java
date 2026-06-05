package com.chicamax.sentinella.iam.domain.model.commands;

import java.util.UUID;

public record UpdateProfileCommand(
        UUID userId,
        String fullName,
        String jobTitle,
        String phone,
        String currentPassword,
        String newPassword
) {
}
