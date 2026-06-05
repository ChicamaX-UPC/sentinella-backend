package com.chicamax.sentinella.shared.infrastructure.websocket;

import com.chicamax.sentinella.iam.domain.services.TokenService;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    public JwtHandshakeInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
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
            var decoded = tokenService.decode(token);
            attributes.put("userId", decoded.userId().toString());
            attributes.put("role", decoded.role().name());
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
