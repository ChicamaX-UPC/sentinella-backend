package com.chicamax.sentinella.iam.domain.model.valueobjects;

public record AuthTokens(String token, String refreshToken, long expiresInSeconds) {
}
