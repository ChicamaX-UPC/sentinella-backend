package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.infrastructure.security.WebSocketTicketService;
import com.chicamax.sentinella.iam.interfaces.rest.resources.ConsumeWsTicketResource;
import com.chicamax.sentinella.iam.interfaces.rest.resources.WsTicketConsumedResource;
import com.chicamax.sentinella.shared.infrastructure.security.InternalServiceAuth;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/internal/ws-tickets")
public class InternalWsTicketController {

    private final WebSocketTicketService webSocketTicketService;
    private final String internalServiceKey;
    private final boolean requireKey;

    public InternalWsTicketController(
            WebSocketTicketService webSocketTicketService,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey,
            @Value("${sentinella.internal.require-key:false}") boolean requireKey
    ) {
        this.webSocketTicketService = webSocketTicketService;
        this.internalServiceKey = internalServiceKey;
        this.requireKey = requireKey;
    }

    @PostMapping("/consume")
    public ResponseEntity<WsTicketConsumedResource> consume(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @Valid @RequestBody ConsumeWsTicketResource resource
    ) {
        InternalServiceAuth.require(internalServiceKey, serviceKey, requireKey);
        var entry = webSocketTicketService.consume(resource.ticket());
        if (entry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket invalido o expirado");
        }
        return ResponseEntity.ok(new WsTicketConsumedResource(entry.userId(), entry.role()));
    }
}
