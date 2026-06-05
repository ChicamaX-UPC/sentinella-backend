package com.chicamax.sentinella.nodeadmin;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nodes/health")
public class NodeAdminHealthController {
    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-nodeadmin-service", "status", "ok");
    }
}
