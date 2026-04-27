package com.bestorigin.monolith.adminplatform.impl.controller;

import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AdminPlatformErrorResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AlertResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventPage;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationStatus;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiDashboardResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportResponse;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformAccessDeniedException;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformValidationException;
import com.bestorigin.monolith.adminplatform.impl.service.AdminPlatformService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/platform")
public class AdminPlatformController {

    private final AdminPlatformService service;

    public AdminPlatformController(AdminPlatformService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    public KpiDashboardResponse kpis(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String period, @RequestParam(required = false) String campaignCode, @RequestParam(required = false) String region, @RequestParam(required = false) String channel) {
        return service.kpis(token(headers), period, campaignCode, region, channel);
    }

    @GetMapping("/integrations")
    public List<IntegrationStatus> integrations(@RequestHeader HttpHeaders headers) {
        return service.integrations(token(headers));
    }

    @PutMapping("/integrations/{integrationCode}")
    public IntegrationSettingsResponse saveIntegration(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable String integrationCode, @RequestBody IntegrationSettingsRequest request) {
        return service.saveIntegration(token(headers), integrationCode, idempotencyKey, request);
    }

    @GetMapping("/audit-events")
    public AuditEventPage auditEvents(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String actor, @RequestParam(required = false) String domain, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.auditEvents(token(headers), actor, domain, actionCode, correlationId, page, size);
    }

    @PostMapping("/reports/exports")
    public ResponseEntity<ReportExportResponse> startExport(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ReportExportRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.startExport(token(headers), idempotencyKey, request));
    }

    @GetMapping("/alerts")
    public List<AlertResponse> alerts(@RequestHeader HttpHeaders headers) {
        return service.alerts(token(headers));
    }

    @ExceptionHandler(AdminPlatformAccessDeniedException.class)
    public ResponseEntity<AdminPlatformErrorResponse> handleForbidden(AdminPlatformAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminPlatformValidationException.class)
    public ResponseEntity<AdminPlatformErrorResponse> handleValidation(AdminPlatformValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminPlatformErrorResponse error(String messageCode, List<String> details) {
        return new AdminPlatformErrorResponse(messageCode, "CORR-036-ERROR", details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return value == null ? "" : value.replace("Bearer ", "").trim();
    }
}