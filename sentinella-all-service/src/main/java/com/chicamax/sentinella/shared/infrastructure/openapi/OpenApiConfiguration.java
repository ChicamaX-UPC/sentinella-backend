package com.chicamax.sentinella.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI sentinellaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sentinella API")
                        .description(
                                "API REST v1. Pulsa Authorize, pega el access token (solo el JWT, sin prefijo Bearer) "
                                        + "tras iniciar sesion en POST /v1/auth/login. La pagina de Swagger es publica; "
                                        + "los candados indican que la API exige ese token en las peticiones.")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT devuelto por POST /v1/auth/login")))
                .security(List.of(new SecurityRequirement().addList("bearer-jwt")));
    }
}
