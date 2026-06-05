package com.chicamax.sentinella.plantmanagement.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterOperatorResource(
        @NotNull UUID ownerId,
        @NotBlank String fullName,
        @NotBlank @Email String email
) {
}
