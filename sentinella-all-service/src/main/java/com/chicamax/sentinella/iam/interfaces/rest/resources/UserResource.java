package com.chicamax.sentinella.iam.interfaces.rest.resources;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResource(
        UUID id,
        String email,
        String fullName,
        Role role,
        UUID[] tailingDamIds,
        boolean active,
        OffsetDateTime lastLogin
) {
}
