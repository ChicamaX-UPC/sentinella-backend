package com.chicamax.sentinella.alerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.alerts", "com.chicamax.sentinella.shared"})
@EnableScheduling
public class AlertsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertsServiceApplication.class, args);
    }
}
