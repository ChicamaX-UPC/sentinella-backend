package com.chicamax.sentinella.dashboard.infrastructure.websocket;

import com.chicamax.sentinella.dashboard.infrastructure.integration.IamWsTicketClient;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private final IamWsTicketClient iamWsTicketClient;

    public JwtHandshakeInterceptor(JwtDecoder jwtDecoder, IamWsTicketClient iamWsTicketClient) {
        this.jwtDecoder = jwtDecoder;
        this.iamWsTicketClient = iamWsTicketClient;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String ticket = queryParam(request.getURI(), "ticket");
        if (ticket != null && !ticket.isBlank()) {
            return iamWsTicketClient.consume(ticket)
                    .map(consumed -> {
                        attributes.put("userId", consumed.userId().toString());
                        if (consumed.role() != null && !consumed.role().isBlank()) {
                            attributes.put("role", consumed.role());
                        }
                        return true;
                    })
                    .orElse(false);
        }

        String token = queryParam(request.getURI(), "token");
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

    private static String queryParam(URI uri, String name) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            return null;
        }
        Optional<String> pair = java.util.Arrays.stream(uri.getQuery().split("&"))
                .filter(part -> part.startsWith(name + "="))
                .findFirst();
        return pair.map(part -> URLDecoder.decode(part.substring(name.length() + 1), StandardCharsets.UTF_8))
                .orElse(null);
    }
}
