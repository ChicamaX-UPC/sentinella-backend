package com.chicamax.sentinella.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.blockchain", "com.chicamax.sentinella.shared"})
@EnableRetry
public class BlockchainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainServiceApplication.class, args);
    }
}
