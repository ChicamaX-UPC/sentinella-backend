package com.chicamax.sentinella.iam.domain.model.valueobjects;

import java.util.UUID;

public record DecodedToken(UUID userId, Role role, TokenType type) {
}
