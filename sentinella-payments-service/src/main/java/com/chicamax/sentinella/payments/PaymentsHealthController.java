package com.chicamax.sentinella.payments;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentsHealthController {

    @GetMapping("/v1/payments/health")
    public Map<String, String> health() {
        return Map.of("service", "sentinella-payments-service", "status", "ok");
    }
}
