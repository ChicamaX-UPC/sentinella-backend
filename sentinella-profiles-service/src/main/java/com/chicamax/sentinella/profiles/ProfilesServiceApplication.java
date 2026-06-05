package com.chicamax.sentinella.profiles;

import com.chicamax.sentinella.shared.infrastructure.openapi.OpenApiConfiguration;
import com.chicamax.sentinella.shared.infrastructure.web.AppCorsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.profiles", "com.chicamax.sentinella.shared"})
@Import({OpenApiConfiguration.class, AppCorsConfiguration.class})
public class ProfilesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfilesServiceApplication.class, args);
    }
}
