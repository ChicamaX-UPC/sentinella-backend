package com.chicamax.sentinella.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.reports", "com.chicamax.sentinella.shared"})
public class ReportsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportsServiceApplication.class, args);
    }
}
