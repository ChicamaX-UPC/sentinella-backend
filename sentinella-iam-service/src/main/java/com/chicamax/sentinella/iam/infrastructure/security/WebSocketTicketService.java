package com.chicamax.sentinella.iam.infrastructure.security;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebSocketTicketService {

    private final long ttlSeconds;
    private final Map<String, TicketEntry> tickets = new ConcurrentHashMap<>();

    public WebSocketTicketService(@Value("${sentinella.auth.ws-ticket-ttl-seconds:30}") long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(UUID userId, String role) {
        purgeExpired();
        String ticket = UUID.randomUUID().toString();
        tickets.put(ticket, new TicketEntry(userId, role, Instant.now().plusSeconds(ttlSeconds)));
        return ticket;
    }

    public TicketEntry consume(String ticket) {
        purgeExpired();
        TicketEntry entry = tickets.remove(ticket);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return null;
        }
        return entry;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        tickets.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    public record TicketEntry(UUID userId, String role, Instant expiresAt) {
    }
}
