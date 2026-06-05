package com.chicamax.sentinella.dashboard;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard/health")
public class DashboardHealthController {
    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-dashboard-service", "status", "ok");
    }
}
