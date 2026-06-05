package com.chicamax.sentinella.iam.interfaces.rest.resources;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleResource(@NotNull Role role) {
}
