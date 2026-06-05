package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.infrastructure.tokens.jwt.JwtKeyProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWKS público para que los demás microservicios configuren {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}.
 */
@RestController
@RequestMapping("/v1/auth")
public class JwtJwksEndpoint {

    private static final String KEY_ID = "sentinella-iam";

    private final JwtKeyProvider jwtKeyProvider;

    public JwtJwksEndpoint(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        try {
            RSAKey rsaKey = new RSAKey.Builder(jwtKeyProvider.getPublicKey())
                    .keyID(KEY_ID)
                    .build();
            JWKSet jwkSet = new JWKSet(rsaKey.toPublicJWK());
            return jwkSet.toJSONObject();
        } catch (Exception ex) {
            throw new IllegalStateException("JWKS incompatible", ex);
        }
    }
}
