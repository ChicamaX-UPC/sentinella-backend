package com.chicamax.sentinella.iam.interfaces.rest.resources;

public record TokenResource(String token, String refreshToken, long expiresIn) {
}
