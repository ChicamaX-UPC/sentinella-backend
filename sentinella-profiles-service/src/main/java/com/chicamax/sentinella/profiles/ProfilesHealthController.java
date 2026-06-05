package com.chicamax.sentinella.profiles;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfilesHealthController {

    @GetMapping("/v1/profiles/health")
    public Map<String, String> health() {
        return Map.of("service", "sentinella-profiles-service", "status", "ok");
    }
}
