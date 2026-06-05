package com.chicamax.sentinella.simulations;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulations/health")
public class SimulationsHealthController {
    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-simulations-service", "status", "ok");
    }
}
