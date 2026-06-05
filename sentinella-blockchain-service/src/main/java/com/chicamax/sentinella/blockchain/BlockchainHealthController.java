package com.chicamax.sentinella.blockchain;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/blockchain/health")
public class BlockchainHealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of("service", "sentinella-blockchain-service", "status", "ok");
    }
}
