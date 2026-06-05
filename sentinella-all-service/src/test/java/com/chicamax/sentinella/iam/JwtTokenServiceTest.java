package com.chicamax.sentinella.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.model.valueobjects.TokenType;
import com.chicamax.sentinella.iam.infrastructure.tokens.jwt.JwtConfig;
import com.chicamax.sentinella.iam.infrastructure.tokens.jwt.JwtKeyProvider;
import com.chicamax.sentinella.iam.infrastructure.tokens.jwt.JwtTokenService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    @Test
    void issueAndDecodeTokens() {
        JwtKeyProvider keyProvider = new JwtKeyProvider("", "");
        JwtConfig jwtConfig = new JwtConfig();
        JwtTokenService tokenService = new JwtTokenService(
                jwtConfig.jwtEncoder(keyProvider),
                jwtConfig.jwtDecoder(keyProvider),
                900,
                604800
        );

        User user = new User(
                UUID.randomUUID(),
                "manager@sentinella.io",
                "hash",
                "Plant Manager",
                Role.PLANT_MANAGER,
                new UUID[0]
        );

        var tokens = tokenService.issueTokens(user);
        var access = tokenService.decode(tokens.token());
        var refresh = tokenService.decode(tokens.refreshToken());

        assertEquals(user.getId(), access.userId());
        assertEquals(Role.PLANT_MANAGER, access.role());
        assertEquals(TokenType.ACCESS, access.type());
        assertEquals(TokenType.REFRESH, refresh.type());
    }
}
