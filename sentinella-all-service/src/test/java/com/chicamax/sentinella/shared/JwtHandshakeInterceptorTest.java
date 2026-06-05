package com.chicamax.sentinella.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.iam.domain.model.valueobjects.DecodedToken;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.model.valueobjects.TokenType;
import com.chicamax.sentinella.iam.domain.services.TokenService;
import com.chicamax.sentinella.shared.infrastructure.websocket.JwtHandshakeInterceptor;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

@ExtendWith(MockitoExtension.class)
class JwtHandshakeInterceptorTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler wsHandler;

    @Test
    void shouldAcceptHandshakeWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();
        when(request.getURI()).thenReturn(URI.create("ws://localhost/v1/ws?token=abc"));
        when(tokenService.decode("abc")).thenReturn(new DecodedToken(userId, Role.PLANT_MANAGER, TokenType.ACCESS));

        JwtHandshakeInterceptor interceptor = new JwtHandshakeInterceptor(tokenService);
        Map<String, Object> attributes = new HashMap<>();
        boolean accepted = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertTrue(accepted);
        assertEquals(userId.toString(), attributes.get("userId"));
        assertEquals(Role.PLANT_MANAGER.name(), attributes.get("role"));
    }

    @Test
    void shouldRejectHandshakeWhenTokenIsMissing() {
        when(request.getURI()).thenReturn(URI.create("ws://localhost/v1/ws"));
        JwtHandshakeInterceptor interceptor = new JwtHandshakeInterceptor(tokenService);

        boolean accepted = interceptor.beforeHandshake(request, response, wsHandler, new HashMap<>());

        assertFalse(accepted);
    }
}
