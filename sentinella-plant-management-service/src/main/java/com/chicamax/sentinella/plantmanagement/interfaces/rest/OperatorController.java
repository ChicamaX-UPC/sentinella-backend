package com.chicamax.sentinella.plantmanagement.interfaces.rest;

import com.chicamax.sentinella.plantmanagement.domain.services.OperatorCommandService;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.OperatorResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RegisterOperatorResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.transform.OperatorAssembler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/operators")
public class OperatorController {

    private final OperatorCommandService operatorCommandService;
    private final OperatorAssembler operatorAssembler;

    public OperatorController(OperatorCommandService operatorCommandService, OperatorAssembler operatorAssembler) {
        this.operatorCommandService = operatorCommandService;
        this.operatorAssembler = operatorAssembler;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<OperatorResource> register(@Valid @RequestBody RegisterOperatorResource resource) {
        var created = operatorCommandService.register(operatorAssembler.toCommand(resource));
        return ResponseEntity.ok(operatorAssembler.toResource(created));
    }
}
