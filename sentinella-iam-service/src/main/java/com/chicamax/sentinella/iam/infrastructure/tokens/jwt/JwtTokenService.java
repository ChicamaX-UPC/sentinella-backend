package com.chicamax.sentinella.iam.infrastructure.tokens.jwt;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import com.chicamax.sentinella.iam.domain.model.valueobjects.DecodedToken;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.model.valueobjects.TokenType;
import com.chicamax.sentinella.iam.domain.services.TokenService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessExpirySeconds;
    private final long refreshExpirySeconds;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            @Value("${jwt.access-expiry}") long accessExpirySeconds,
            @Value("${jwt.refresh-expiry}") long refreshExpirySeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.accessExpirySeconds = accessExpirySeconds;
        this.refreshExpirySeconds = refreshExpirySeconds;
    }

    @Override
    public AuthTokens issueTokens(User user) {
        String accessToken = encode(user, TokenType.ACCESS, accessExpirySeconds);
        String refreshToken = encode(user, TokenType.REFRESH, refreshExpirySeconds);
        return new AuthTokens(accessToken, refreshToken, accessExpirySeconds);
    }

    @Override
    public DecodedToken decode(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        UUID userId = UUID.fromString(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        TokenType type = TokenType.valueOf(jwt.getClaimAsString("typ"));
        return new DecodedToken(userId, role, type, jwt.getExpiresAt());
    }

    private String encode(User user, TokenType type, long expirySeconds) {
        Instant now = Instant.now();
        List<String> damIds = user.getTailingDamIds() == null
                ? List.of()
                : Arrays.stream(user.getTailingDamIds()).map(UUID::toString).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .claim("role", user.getRole().name())
                .claim("organizationId", user.getOrganizationId().toString())
                .claim("damIds", damIds)
                .claim("typ", type.name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
