package com.chicamax.sentinella.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileResource(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 100) String jobTitle,
        @Size(max = 30) String phone,
        String currentPassword,
        @Size(min = 8, max = 128) String newPassword
) {
}
