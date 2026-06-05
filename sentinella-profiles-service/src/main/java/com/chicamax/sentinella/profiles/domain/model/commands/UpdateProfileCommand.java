package com.chicamax.sentinella.profiles.domain.model.commands;

import java.util.UUID;

public record UpdateProfileCommand(
        UUID userId,
        String fullName,
        String phone,
        String jobTitle,
        String preferencesJson
) {
}
