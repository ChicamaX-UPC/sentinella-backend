package com.chicamax.sentinella.iam;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/health")
public class AuthHealthController {
    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-iam-service", "status", "ok");
    }
}
