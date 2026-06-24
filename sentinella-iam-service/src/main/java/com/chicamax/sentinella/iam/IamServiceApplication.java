package com.chicamax.sentinella.iam;

import com.chicamax.sentinella.shared.infrastructure.openapi.OpenApiConfiguration;
import com.chicamax.sentinella.shared.infrastructure.security.SentinellaResourceServerSecurityConfig;
import com.chicamax.sentinella.shared.infrastructure.web.AppCorsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.chicamax.sentinella.iam", "com.chicamax.sentinella.shared"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SentinellaResourceServerSecurityConfig.class
        )
)
@Import({OpenApiConfiguration.class, AppCorsConfiguration.class})
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}
