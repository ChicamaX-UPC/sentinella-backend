package com.chicamax.sentinella.iam.domain.model.commands;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import java.util.UUID;

public record UpdateUserRoleCommand(UUID userId, Role role) {
}
