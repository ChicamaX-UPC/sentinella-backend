package com.chicamax.sentinella.shared.interfaces.rest;

import java.util.List;

/** Respuesta paginada estándar para listados REST. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int safeSize = Math.max(size, 1);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        int safePage = Math.max(page, 0);
        return new PageResponse<>(
                content,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                safePage == 0,
                totalPages == 0 || safePage >= totalPages - 1
        );
    }
}
