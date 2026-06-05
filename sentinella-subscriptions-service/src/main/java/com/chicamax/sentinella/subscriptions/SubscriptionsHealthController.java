package com.chicamax.sentinella.subscriptions;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionsHealthController {

    @GetMapping("/v1/subscriptions/health")
    public Map<String, String> health() {
        return Map.of("service", "sentinella-subscriptions-service", "status", "ok");
    }
}
