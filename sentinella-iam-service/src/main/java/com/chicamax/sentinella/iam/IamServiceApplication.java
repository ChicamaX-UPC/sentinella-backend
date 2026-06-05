package com.chicamax.sentinella.iam;

import com.chicamax.sentinella.shared.infrastructure.web.AppCorsConfiguration;
import com.chicamax.sentinella.shared.infrastructure.openapi.OpenApiConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.chicamax.sentinella.iam")
@Import({OpenApiConfiguration.class, AppCorsConfiguration.class})
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}
