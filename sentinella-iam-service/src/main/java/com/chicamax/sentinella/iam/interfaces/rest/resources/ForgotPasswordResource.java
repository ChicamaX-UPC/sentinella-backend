package com.chicamax.sentinella.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordResource(@Email @NotBlank String email) {
}
