package com.chicamax.sentinella.iam.domain.model.valueobjects;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RolePermissionDefaults {

    private RolePermissionDefaults() {
    }

    public static Set<Permission> forRole(Role role) {
        return switch (role) {
            case SYSTEM_ADMIN -> EnumSet.allOf(Permission.class);
            case PLANT_MANAGER -> EnumSet.of(
                    Permission.VIEW_READINGS,
                    Permission.CONFIGURE_THRESHOLDS,
                    Permission.MANAGE_ALERTS,
                    Permission.CLOSE_ALERTS,
                    Permission.REGISTER_ROUNDS,
                    Permission.GENERATE_REPORTS,
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_SIMULATIONS,
                    Permission.VIEW_SIMULATIONS,
                    Permission.RUN_SIMULATIONS
            );
            case FIELD_OPERATOR -> EnumSet.of(
                    Permission.VIEW_READINGS,
                    Permission.MANAGE_ALERTS,
                    Permission.REGISTER_ROUNDS
            );
            case READ_ONLY -> EnumSet.of(
                    Permission.VIEW_READINGS,
                    Permission.GENERATE_REPORTS,
                    Permission.VIEW_SIMULATIONS
            );
        };
    }

    public static String[] toArray(Role role) {
        return forRole(role).stream().map(Permission::name).toArray(String[]::new);
    }

    public static String[] normalize(String[] stored, Role role) {
        if (stored != null && stored.length > 0) {
            return Arrays.stream(stored)
                    .filter(RolePermissionDefaults::isValid)
                    .distinct()
                    .toArray(String[]::new);
        }
        return toArray(role);
    }

    public static boolean isValid(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        try {
            Permission.valueOf(name.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static String[] validateAndDistinct(String[] requested) {
        if (requested == null || requested.length == 0) {
            return new String[0];
        }
        return Arrays.stream(requested)
                .filter(RolePermissionDefaults::isValid)
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }

    public static Set<Permission> parseEffective(String[] stored, Role role) {
        String[] effective = normalize(stored, role);
        return Arrays.stream(effective)
                .map(Permission::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
    }
}
