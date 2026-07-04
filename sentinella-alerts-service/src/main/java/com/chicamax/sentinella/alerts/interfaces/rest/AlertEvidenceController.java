package com.chicamax.sentinella.alerts.interfaces.rest;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertEvidence;
import com.chicamax.sentinella.alerts.domain.model.events.AlertEvidenceUploadedEvent;
import com.chicamax.sentinella.alerts.domain.services.AlertQueryService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertEvidenceRepository;
import com.chicamax.sentinella.alerts.infrastructure.storage.AlertEvidenceStorageService;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertEvidenceResource;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/alerts/{alertId}/evidence")
public class AlertEvidenceController {

    private final AlertQueryService alertQueryService;
    private final AlertEvidenceRepository alertEvidenceRepository;
    private final AlertEvidenceStorageService alertEvidenceStorageService;
    private final ApplicationEventPublisher eventPublisher;

    public AlertEvidenceController(
            AlertQueryService alertQueryService,
            AlertEvidenceRepository alertEvidenceRepository,
            AlertEvidenceStorageService alertEvidenceStorageService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.alertQueryService = alertQueryService;
        this.alertEvidenceRepository = alertEvidenceRepository;
        this.alertEvidenceStorageService = alertEvidenceStorageService;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping
    public ResponseEntity<List<AlertEvidenceResource>> list(@PathVariable UUID alertId) {
        ensureAlertExists(alertId);
        var items = alertEvidenceRepository.findByAlertIdOrderByUploadedAtDesc(alertId).stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(items);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<AlertEvidenceResource> upload(
            @PathVariable UUID alertId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Alert alert = ensureAlertExists(alertId);
        try {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidence.bin";
            String key = alertEvidenceStorageService.store(alertId, file.getBytes(), filename);
            AlertEvidence evidence = new AlertEvidence(
                    UUID.randomUUID(),
                    alertId,
                    key,
                    file.getContentType(),
                    UUID.fromString(jwt.getSubject())
            );
            AlertEvidence saved = alertEvidenceRepository.save(evidence);
            eventPublisher.publishEvent(new AlertEvidenceUploadedEvent(
                    saved.getId(),
                    alertId,
                    alert.getNodeId(),
                    saved.getStorageKey(),
                    saved.getContentType(),
                    saved.getUploadedBy(),
                    saved.getUploadedAt()
            ));
            return ResponseEntity.ok(toResource(saved));
        } catch (java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo invalido");
        }
    }

    private Alert ensureAlertExists(UUID alertId) {
        return alertQueryService.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));
    }

    private AlertEvidenceResource toResource(AlertEvidence evidence) {
        return new AlertEvidenceResource(
                evidence.getId(),
                evidence.getAlertId(),
                evidence.getStorageKey(),
                evidence.getContentType(),
                evidence.getUploadedBy(),
                evidence.getUploadedAt()
        );
    }
}
