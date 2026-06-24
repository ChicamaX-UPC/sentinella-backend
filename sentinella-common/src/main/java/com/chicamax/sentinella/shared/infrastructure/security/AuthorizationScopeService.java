package com.chicamax.sentinella.shared.infrastructure.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Alcance multi-tenant: organización + tranques ({@code damIds} en JWT).
 * {@code SYSTEM_ADMIN} es administrador de su empresa, no acceso global a la plataforma.
 */
@Component
public class AuthorizationScopeService {

    public boolean canAccessDam(Jwt jwt, UUID damId) {
        if (jwt == null || damId == null) {
            return false;
        }
        Set<UUID> allowed = extractDamIds(jwt);
        if (allowed.isEmpty()) {
            return false;
        }
        return allowed.contains(damId);
    }

    /** Siempre filtrar listados de nodos/alertas por tranques del JWT. */
    public boolean shouldScopeByDam(Jwt jwt) {
        return true;
    }

    public boolean canAccessOrganization(Jwt jwt, UUID organizationId) {
        if (jwt == null || organizationId == null) {
            return false;
        }
        UUID claimOrg = extractOrganizationId(jwt);
        return claimOrg != null && claimOrg.equals(organizationId);
    }

    public UUID requireOrganizationId(Jwt jwt) {
        UUID org = extractOrganizationId(jwt);
        if (org == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Organización no identificada");
        }
        return org;
    }

    public void ensureOrganizationAccess(Jwt jwt, UUID organizationId) {
        if (!canAccessOrganization(jwt, organizationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a la organización");
        }
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

    public UUID extractOrganizationId(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        String raw = jwt.getClaimAsString("organizationId");
        return parseUuid(raw);
    }

    public boolean isSystemAdmin(Jwt jwt) {
        String role = jwt != null ? jwt.getClaimAsString("role") : null;
        return "SYSTEM_ADMIN".equals(role);
    }

    public boolean canManageUsers(Jwt jwt) {
        if (jwt == null) {
            return false;
        }
        if (isSystemAdmin(jwt)) {
            return true;
        }
        String role = jwt.getClaimAsString("role");
        return "PLANT_MANAGER".equals(role);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
