package com.chicamax.sentinella.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserDetailsResource(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 100) String jobTitle,
        @Size(max = 30) String phone
) {
}
