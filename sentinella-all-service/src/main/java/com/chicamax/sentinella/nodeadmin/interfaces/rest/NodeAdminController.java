package com.chicamax.sentinella.nodeadmin.interfaces.rest;

import com.chicamax.sentinella.nodeadmin.domain.services.IoTNodeCommandService;
import com.chicamax.sentinella.nodeadmin.interfaces.rest.resources.NodeResource;
import com.chicamax.sentinella.nodeadmin.interfaces.rest.resources.RegisterNodeResource;
import com.chicamax.sentinella.nodeadmin.interfaces.rest.transform.IoTNodeAssembler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/nodes")
public class NodeAdminController {

    private final IoTNodeCommandService ioTNodeCommandService;
    private final IoTNodeAssembler ioTNodeAssembler;

    public NodeAdminController(IoTNodeCommandService ioTNodeCommandService, IoTNodeAssembler ioTNodeAssembler) {
        this.ioTNodeCommandService = ioTNodeCommandService;
        this.ioTNodeAssembler = ioTNodeAssembler;
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<NodeResource> register(@Valid @RequestBody RegisterNodeResource resource) {
        var created = ioTNodeCommandService.register(ioTNodeAssembler.toCommand(resource));
        return ResponseEntity.ok(ioTNodeAssembler.toResource(created));
    }
}
