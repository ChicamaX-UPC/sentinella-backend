package com.chicamax.sentinella.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthRateLimiter {

    private final int maxAttempts;
    private final long windowMs;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public AuthRateLimiter(
            @Value("${sentinella.auth.rate-limit.max-attempts:20}") int maxAttempts,
            @Value("${sentinella.auth.rate-limit.window-ms:60000}") long windowMs
    ) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
    }

    public void check(HttpServletRequest request) {
        String key = clientKey(request);
        long now = System.currentTimeMillis();
        AttemptWindow window = attempts.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMs > windowMs) {
                return new AttemptWindow(now, 1);
            }
            existing.count++;
            return existing;
        });
        if (window.count > maxAttempts) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos, espere un momento");
        }
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class AttemptWindow {
        private final long startMs;
        private int count;

        private AttemptWindow(long startMs, int count) {
            this.startMs = startMs;
            this.count = count;
        }
    }
}
