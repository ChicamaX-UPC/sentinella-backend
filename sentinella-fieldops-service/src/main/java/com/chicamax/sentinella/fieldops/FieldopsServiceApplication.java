package com.chicamax.sentinella.fieldops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.fieldoperations", "com.chicamax.sentinella.shared"})
@EntityScan(basePackages = "com.chicamax.sentinella.fieldoperations.domain.model")
@EnableJpaRepositories(basePackages = "com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa")
public class FieldopsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FieldopsServiceApplication.class, args);
    }
}
