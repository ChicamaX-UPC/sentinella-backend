package com.chicamax.sentinella.plantmanagement;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plant-management/health")
public class PlantManagementHealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-plant-management-service", "status", "ok");
    }
}
