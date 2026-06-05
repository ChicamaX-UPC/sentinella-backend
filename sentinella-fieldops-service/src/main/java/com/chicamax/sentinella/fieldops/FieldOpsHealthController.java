package com.chicamax.sentinella.fieldops;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rounds/health")
public class FieldOpsHealthController {
    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-fieldops-service", "status", "ok");
    }
}
