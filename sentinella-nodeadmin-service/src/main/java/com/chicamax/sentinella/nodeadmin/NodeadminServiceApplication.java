package com.chicamax.sentinella.nodeadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.chicamax.sentinella.nodeadmin", "com.chicamax.sentinella.shared"})
public class NodeadminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeadminServiceApplication.class, args);
    }
}
