package com.bestorigin.monolith.admincatalog.impl.controller;

import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.AdminCatalogErrorResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.ArchiveResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.AuditResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.CampaignCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.CampaignListResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.CampaignResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.HotspotCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.HotspotResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.IssueCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.IssueResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.LinkValidationResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.MaterialCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.MaterialResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.PageCreateRequest;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.PageResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.RolloverResponse;
import com.bestorigin.monolith.admincatalog.api.AdminCatalogDtos.WorkspaceResponse;
import com.bestorigin.monolith.admincatalog.impl.exception.AdminCatalogAccessDeniedException;
import com.bestorigin.monolith.admincatalog.impl.exception.AdminCatalogConflictException;
import com.bestorigin.monolith.admincatalog.impl.exception.AdminCatalogValidationException;
import com.bestorigin.monolith.admincatalog.impl.service.AdminCatalogService;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin-catalog")
public class AdminCatalogController {

    private final AdminCatalogService service;

    public AdminCatalogController(AdminCatalogService service) {
        this.service = service;
    }

    @GetMapping("/workspace")
    public WorkspaceResponse workspace(@RequestHeader HttpHeaders headers) {
        return service.workspace(token(headers));
    }

    @GetMapping("/campaigns")
    public CampaignListResponse campaigns(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String locale, @RequestParam(required = false) String search) {
        return service.searchCampaigns(token(headers), status, locale, search);
    }

    @PostMapping("/campaigns")
    public ResponseEntity<CampaignResponse> createCampaign(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody CampaignCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCampaign(token(headers), idempotencyKey, request));
    }

    @PostMapping("/campaigns/{campaignId}/issues")
    public ResponseEntity<IssueResponse> createIssue(@RequestHeader HttpHeaders headers, @PathVariable UUID campaignId, @RequestBody IssueCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createIssue(token(headers), campaignId, request));
    }

    @PostMapping("/issues/{issueId}/materials")
    public ResponseEntity<MaterialResponse> addMaterial(@RequestHeader HttpHeaders headers, @PathVariable UUID issueId, @RequestBody MaterialCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMaterial(token(headers), issueId, request));
    }

    @PostMapping("/materials/{materialId}/approve")
    public MaterialResponse approveMaterial(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId) {
        return service.approveMaterial(token(headers), materialId);
    }

    @PostMapping("/issues/{issueId}/pages")
    public ResponseEntity<PageResponse> addPage(@RequestHeader HttpHeaders headers, @PathVariable UUID issueId, @RequestBody PageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPage(token(headers), issueId, request));
    }

    @PostMapping("/issues/{issueId}/hotspots")
    public ResponseEntity<HotspotResponse> addHotspot(@RequestHeader HttpHeaders headers, @PathVariable UUID issueId, @RequestBody HotspotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addHotspot(token(headers), issueId, request));
    }

    @PostMapping("/issues/{issueId}/validate-links")
    public LinkValidationResponse validateLinks(@RequestHeader HttpHeaders headers, @PathVariable UUID issueId) {
        return service.validateLinks(token(headers), issueId);
    }

    @PostMapping("/issues/{issueId}/rollover")
    public RolloverResponse rollover(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID issueId) {
        return service.rollover(token(headers), issueId, idempotencyKey);
    }

    @GetMapping("/archive")
    public ArchiveResponse archive(@RequestHeader HttpHeaders headers) {
        return service.archive(token(headers));
    }

    @GetMapping("/audit")
    public AuditResponse audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String correlationId) {
        return service.audit(token(headers), actionCode, correlationId);
    }

    @ExceptionHandler(AdminCatalogAccessDeniedException.class)
    public ResponseEntity<AdminCatalogErrorResponse> handleForbidden(AdminCatalogAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminCatalogConflictException.class)
    public ResponseEntity<AdminCatalogErrorResponse> handleConflict(AdminCatalogConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminCatalogValidationException.class)
    public ResponseEntity<AdminCatalogErrorResponse> handleValidation(AdminCatalogValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminCatalogErrorResponse error(String messageCode, java.util.List<String> details) {
        return new AdminCatalogErrorResponse(messageCode, "CORR-030-ERROR", details == null ? java.util.List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
