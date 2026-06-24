package com.chicamax.sentinella.iam.domain.model.commands;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import java.util.UUID;

public record CreateUserCommand(
        String email,
        String password,
        String fullName,
        Role role,
        UUID organizationId,
        UUID[] tailingDamIds
) {
}
