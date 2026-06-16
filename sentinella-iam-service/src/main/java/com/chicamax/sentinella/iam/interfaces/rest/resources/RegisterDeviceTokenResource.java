package com.chicamax.sentinella.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceTokenResource(
        @NotBlank String token,
        @NotBlank String platform
) {
}
