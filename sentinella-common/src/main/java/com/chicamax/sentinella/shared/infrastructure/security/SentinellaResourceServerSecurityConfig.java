package com.chicamax.sentinella.shared.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * JWT vía JWKS del IAM ({@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}). No usar en iam-service (tiene su propia cadena HTTP).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SentinellaResourceServerSecurityConfig {

    private final boolean openapiPublic;

    public SentinellaResourceServerSecurityConfig(
            @Value("${sentinella.openapi.public:true}") boolean openapiPublic
    ) {
        this.openapiPublic = openapiPublic;
    }

    @Bean
    SecurityFilterChain sentinellaResourceServerChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info", "/actuator/prometheus")
                            .permitAll();
                    if (openapiPublic) {
                        auth.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                                .permitAll();
                    }
                    auth.requestMatchers(new AntPathRequestMatcher("/v1/ws/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/v1/internal/**")).permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(sentinellaBearerTokenResolver())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    /**
     * En rutas públicas/internas no exige Bearer: evita 401 del resource server en POST sin JWT
     * (p. ej. ingesta edge con {@code X-Internal-Service-Key}).
     */
    @Bean
    BearerTokenResolver sentinellaBearerTokenResolver() {
        return request -> {
            if (isPublicOrInternal(request)) {
                return null;
            }
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return authorization.substring(7).trim();
            }
            return null;
        };
    }

    private static boolean isPublicOrInternal(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v1/internal/")
                || path.startsWith("/v1/ws/")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info")
                || path.equals("/actuator/prometheus");
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwtToken -> new JwtAuthenticationToken(jwtToken, extractAuthorities(jwtToken));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwtToken) {
        String role = jwtToken.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
