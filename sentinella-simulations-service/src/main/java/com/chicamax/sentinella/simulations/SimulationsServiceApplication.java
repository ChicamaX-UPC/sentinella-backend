package com.chicamax.sentinella.simulations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.simulations", "com.chicamax.sentinella.shared"})
public class SimulationsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulationsServiceApplication.class, args);
    }
}
