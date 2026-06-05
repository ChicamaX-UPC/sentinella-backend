package com.chicamax.sentinella;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SentinellaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinellaApplication.class, args);
    }
}
