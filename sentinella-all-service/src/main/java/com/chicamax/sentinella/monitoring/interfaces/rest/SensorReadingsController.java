package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.model.queries.GetReadingsByNodeQuery;
import com.chicamax.sentinella.monitoring.domain.services.SensorReadingQueryService;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.SensorReadingResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.transform.SensorReadingAssembler;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/nodes")
public class SensorReadingsController {

    private final SensorReadingQueryService sensorReadingQueryService;
    private final SensorReadingAssembler sensorReadingAssembler;

    public SensorReadingsController(
            SensorReadingQueryService sensorReadingQueryService,
            SensorReadingAssembler sensorReadingAssembler
    ) {
        this.sensorReadingQueryService = sensorReadingQueryService;
        this.sensorReadingAssembler = sensorReadingAssembler;
    }

    @GetMapping("/{nodeId}/readings")
    public ResponseEntity<List<SensorReadingResource>> getReadings(
            @PathVariable UUID nodeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var query = new GetReadingsByNodeQuery(nodeId, from, to, page, Math.min(Math.max(limit, 1), 200));
        List<SensorReadingResource> readings = sensorReadingQueryService.handle(query)
                .stream()
                .map(sensorReadingAssembler::toResource)
                .toList();
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/{nodeId}/status")
    public ResponseEntity<SensorReadingResource> getNodeStatus(@PathVariable UUID nodeId) {
        var latest = sensorReadingQueryService.getLatestByNode(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin lecturas para este nodo"));
        return ResponseEntity.ok(sensorReadingAssembler.toResource(latest));
    }
}
