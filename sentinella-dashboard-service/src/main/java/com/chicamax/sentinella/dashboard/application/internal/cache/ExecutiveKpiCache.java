package com.chicamax.sentinella.dashboard.application.internal.cache;

import com.chicamax.sentinella.dashboard.interfaces.rest.resources.ExecutiveDashboardResource;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExecutiveKpiCache {

    private final long ttlSeconds;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public ExecutiveKpiCache(@Value("${sentinella.dashboard.kpi-cache-ttl-seconds:60}") long ttlSeconds) {
        this.ttlSeconds = Math.max(5, ttlSeconds);
    }

    public Optional<ExecutiveDashboardResource> get(Set<UUID> damIds) {
        purgeExpired();
        Entry entry = entries.get(key(damIds));
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    public void put(Set<UUID> damIds, ExecutiveDashboardResource value) {
        entries.put(key(damIds), new Entry(value, Instant.now().plusSeconds(ttlSeconds)));
    }

    public void invalidateAll() {
        entries.clear();
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        entries.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private static String key(Set<UUID> damIds) {
        if (damIds == null || damIds.isEmpty()) {
            return "_empty";
        }
        return damIds.stream().map(UUID::toString).sorted().collect(Collectors.joining(","));
    }

    private record Entry(ExecutiveDashboardResource value, Instant expiresAt) {
    }
}
