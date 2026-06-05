package com.chicamax.sentinella.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(
        String token,
        String refreshToken,
        long expiresIn,
        UserResource user
) {
}
