package com.chicamax.sentinella.shared.infrastructure.security;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
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
import org.springframework.security.web.SecurityFilterChain;

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
                    auth.requestMatchers("/v1/ws/**").permitAll()
                            .requestMatchers("/v1/internal/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/v1/monitoring/readings/ingest").permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth -> oauth.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .build();
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
