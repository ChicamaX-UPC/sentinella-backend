package com.chicamax.sentinella.reports.interfaces.rest;

import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.queries.GetReportsByDamQuery;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.services.ReportCommandService;
import com.chicamax.sentinella.reports.domain.services.ReportQueryService;
import com.chicamax.sentinella.reports.domain.services.StorageService;
import com.chicamax.sentinella.reports.interfaces.rest.resources.GenerateReportResource;
import com.chicamax.sentinella.reports.interfaces.rest.resources.ReportDownloadResource;
import com.chicamax.sentinella.reports.interfaces.rest.resources.ReportResource;
import com.chicamax.sentinella.reports.interfaces.rest.transform.ReportAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/reports")
public class ReportsController {

    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;
    private final StorageService storageService;
    private final ReportAssembler reportAssembler;
    private final AuthorizationScopeService authorizationScopeService;

    public ReportsController(
            ReportCommandService reportCommandService,
            ReportQueryService reportQueryService,
            StorageService storageService,
            ReportAssembler reportAssembler,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.reportCommandService = reportCommandService;
        this.reportQueryService = reportQueryService;
        this.storageService = storageService;
        this.reportAssembler = reportAssembler;
        this.authorizationScopeService = authorizationScopeService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<ReportResource> generate(
            @Valid @RequestBody GenerateReportResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (resource.tailingDamId() != null && !authorizationScopeService.canAccessDam(jwt, resource.tailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        var report = reportCommandService.generate(reportAssembler.toCommand(resource, userId), jwt.getTokenValue());
        return ResponseEntity.ok(reportAssembler.toResource(report));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<List<ReportResource>> getReports(
            @RequestParam(required = false) UUID tailingDamId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (tailingDamId != null && !authorizationScopeService.canAccessDam(jwt, tailingDamId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        List<ReportResource> reports = reportQueryService.handle(new GetReportsByDamQuery(tailingDamId))
                .stream()
                .filter(report -> report.getTailingDamId() == null || authorizationScopeService.canAccessDam(jwt, report.getTailingDamId()))
                .map(reportAssembler::toResource)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{reportId}/download")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<ReportDownloadResource> download(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var report = reportQueryService.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
        if (report.getTailingDamId() != null && !authorizationScopeService.canAccessDam(jwt, report.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        return ResponseEntity.ok(new ReportDownloadResource(
                report.getId(),
                storageService.getDownloadUri(report.getStorageKey())
        ));
    }

    @GetMapping("/{reportId}/content")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<byte[]> downloadContent(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Report report = reportQueryService.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
        if (report.getTailingDamId() != null && !authorizationScopeService.canAccessDam(jwt, report.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        byte[] body = storageService.readReport(report.getStorageKey());
        String ext = report.getFormat().fileExtension();
        MediaType mediaType = report.getFormat() == ReportFormat.PDF
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + reportId + "." + ext + "\"")
                .body(body);
    }
}
