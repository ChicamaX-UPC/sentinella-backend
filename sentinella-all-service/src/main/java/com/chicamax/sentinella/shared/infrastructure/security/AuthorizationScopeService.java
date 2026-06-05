package com.chicamax.sentinella.shared.infrastructure.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationScopeService {

    public boolean canAccessDam(Jwt jwt, UUID damId) {
        if (jwt == null || damId == null) {
            return false;
        }
        if (isSystemAdmin(jwt)) {
            return true;
        }
        Set<UUID> allowed = extractDamIds(jwt);
        if (allowed.isEmpty()) {
            return true;
        }
        return allowed.contains(damId);
    }

    public Set<UUID> extractDamIds(Jwt jwt) {
        if (jwt == null) {
            return Set.of();
        }
        Object claim = jwt.getClaims().get("damIds");
        if (!(claim instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
                .map(Object::toString)
                .map(this::parseUuid)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean isSystemAdmin(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        return "SYSTEM_ADMIN".equals(role);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
