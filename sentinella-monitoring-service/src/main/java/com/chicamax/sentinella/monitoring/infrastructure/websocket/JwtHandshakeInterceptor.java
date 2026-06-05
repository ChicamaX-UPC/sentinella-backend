package com.chicamax.sentinella.monitoring.infrastructure.websocket;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;

    public JwtHandshakeInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = extractToken(request.getURI());
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwt jwt = jwtDecoder.decode(token);
            attributes.put("userId", jwt.getSubject());
            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                attributes.put("role", role);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }

    private String extractToken(URI uri) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            return null;
        }
        Optional<String> tokenPair = Arrays.stream(uri.getQuery().split("&"))
                .filter(part -> part.startsWith("token="))
                .findFirst();
        return tokenPair.map(part -> part.substring("token=".length())).orElse(null);
    }
}
