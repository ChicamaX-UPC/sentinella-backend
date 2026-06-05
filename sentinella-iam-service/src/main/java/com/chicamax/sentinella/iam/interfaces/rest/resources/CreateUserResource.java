package com.chicamax.sentinella.iam.interfaces.rest.resources;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateUserResource(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String fullName,
        @NotNull Role role,
        UUID[] tailingDamIds
) {
}
