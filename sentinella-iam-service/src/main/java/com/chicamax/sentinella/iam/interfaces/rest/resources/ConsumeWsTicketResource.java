package com.chicamax.sentinella.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record ConsumeWsTicketResource(@NotBlank String ticket) {
}
