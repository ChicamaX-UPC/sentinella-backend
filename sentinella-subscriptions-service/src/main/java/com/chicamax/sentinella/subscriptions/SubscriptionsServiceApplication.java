package com.chicamax.sentinella.subscriptions;

import com.chicamax.sentinella.shared.infrastructure.openapi.OpenApiConfiguration;
import com.chicamax.sentinella.shared.infrastructure.web.AppCorsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.subscriptions", "com.chicamax.sentinella.shared"})
@Import({OpenApiConfiguration.class, AppCorsConfiguration.class})
public class SubscriptionsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionsServiceApplication.class, args);
    }
}
