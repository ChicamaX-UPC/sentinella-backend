package com.chicamax.sentinella.iam.infrastructure.tokens.jwt;

import com.chicamax.sentinella.iam.infrastructure.security.TokenRevocationFilter;
import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Dos cadenas: sin OAuth2 JWT en rutas públicas de auth. Si una petición lleva
 * {@code Authorization: Bearer} inválido, el resource server respondería 401 con cuerpo vacío
 * antes de evaluar {@code permitAll()} (comportamiento documentado de Spring Security 6).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final boolean openapiPublic;

    public SecurityConfig(@Value("${sentinella.openapi.public:true}") boolean openapiPublic) {
        this.openapiPublic = openapiPublic;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicAuthChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(
                        "/v1/auth/login",
                        "/v1/auth/register",
                        "/v1/auth/logout",
                        "/v1/auth/refresh",
                        "/v1/auth/forgot-password",
                        "/v1/auth/reset-password",
                        "/api/v1/auth/health"
                )
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiChain(HttpSecurity http, TokenRevocationFilter tokenRevocationFilter) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/v1/ws/**").permitAll();
                    if (openapiPublic) {
                        auth.requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/swagger-ui/**")
                                .permitAll();
                    }
                    auth.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info", "/actuator/prometheus")
                            .permitAll()
                            .requestMatchers(HttpMethod.GET, "/v1/users/assignable").authenticated()
                            .requestMatchers(HttpMethod.GET, "/v1/users/me").authenticated()
                            .requestMatchers(HttpMethod.PATCH, "/v1/users/me").authenticated()
                            .requestMatchers(HttpMethod.POST, "/v1/users/me/device-tokens").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/v1/users/me/device-tokens").authenticated()
                            .requestMatchers(HttpMethod.POST, "/v1/auth/ws-ticket").authenticated()
                            .requestMatchers("/v1/internal/**").permitAll()
                            .requestMatchers("/v1/users/**").hasAnyRole("SYSTEM_ADMIN", "PLANT_MANAGER")
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .addFilterBefore(tokenRevocationFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> new JwtAuthenticationToken(jwt, extractAuthorities(jwt));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
