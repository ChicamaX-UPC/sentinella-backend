package com.chicamax.sentinella.plantmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.chicamax.sentinella.plantmanagement",
        "com.chicamax.sentinella.fieldoperations",
        "com.chicamax.sentinella.shared"
})
@EntityScan(basePackages = {
        "com.chicamax.sentinella.plantmanagement.domain.model",
        "com.chicamax.sentinella.fieldoperations.domain.model"
})
@EnableJpaRepositories(basePackages = {
        "com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa",
        "com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa"
})
public class PlantManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantManagementServiceApplication.class, args);
    }
}
