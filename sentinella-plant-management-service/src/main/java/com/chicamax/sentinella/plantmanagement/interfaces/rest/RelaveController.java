package com.chicamax.sentinella.plantmanagement.interfaces.rest;

import com.chicamax.sentinella.plantmanagement.domain.services.RelaveCommandService;
import com.chicamax.sentinella.plantmanagement.domain.services.RelaveQueryService;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.CreateRelaveResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RelaveResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.transform.RelaveAssembler;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/relaves")
public class RelaveController {

    private final RelaveCommandService relaveCommandService;
    private final RelaveQueryService relaveQueryService;
    private final RelaveAssembler relaveAssembler;

    public RelaveController(
            RelaveCommandService relaveCommandService,
            RelaveQueryService relaveQueryService,
            RelaveAssembler relaveAssembler
    ) {
        this.relaveCommandService = relaveCommandService;
        this.relaveQueryService = relaveQueryService;
        this.relaveAssembler = relaveAssembler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<List<RelaveResource>> listRelaves() {
        var content = relaveQueryService.findAll().stream().map(relaveAssembler::toResource).toList();
        return ResponseEntity.ok(content);
    }

    @GetMapping("/{relaveId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<RelaveResource> getRelave(@PathVariable UUID relaveId) {
        var relave = relaveQueryService.findById(relaveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relave no encontrado"));
        return ResponseEntity.ok(relaveAssembler.toResource(relave));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<RelaveResource> createRelave(@Valid @RequestBody CreateRelaveResource resource) {
        var created = relaveCommandService.create(relaveAssembler.toCommand(resource));
        return ResponseEntity.ok(relaveAssembler.toResource(created));
    }
}
