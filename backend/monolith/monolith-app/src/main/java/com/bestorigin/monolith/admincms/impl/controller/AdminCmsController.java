package com.bestorigin.monolith.admincms.impl.controller;

import com.bestorigin.monolith.admincms.api.AdminCmsDtos.AdminCmsErrorResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.AuditResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialDetailResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialListResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.MaterialUpsertRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.PreviewResponse;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.PublishRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.ReviewRequest;
import com.bestorigin.monolith.admincms.api.AdminCmsDtos.VersionListResponse;
import com.bestorigin.monolith.admincms.impl.exception.AdminCmsAccessDeniedException;
import com.bestorigin.monolith.admincms.impl.exception.AdminCmsConflictException;
import com.bestorigin.monolith.admincms.impl.exception.AdminCmsValidationException;
import com.bestorigin.monolith.admincms.impl.service.AdminCmsService;
import java.util.UUID;
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
@RequestMapping("/api/admin-cms")
public class AdminCmsController {

    private final AdminCmsService service;

    public AdminCmsController(AdminCmsService service) {
        this.service = service;
    }

    @GetMapping("/materials")
    public MaterialListResponse materials(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String materialType, @RequestParam(required = false) String status, @RequestParam(required = false) String language, @RequestParam(required = false) String search) {
        return service.searchMaterials(token(headers), materialType, status, language, search);
    }

    @PostMapping("/materials")
    public ResponseEntity<MaterialDetailResponse> createMaterial(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @RequestBody MaterialUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMaterial(token(headers), elevatedSessionId, request));
    }

    @GetMapping("/materials/{materialId}")
    public MaterialDetailResponse material(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId) {
        return service.getMaterial(token(headers), materialId);
    }

    @PutMapping("/materials/{materialId}")
    public MaterialDetailResponse updateMaterial(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId, @RequestBody MaterialUpsertRequest request) {
        return service.updateMaterial(token(headers), materialId, request);
    }

    @PostMapping("/materials/{materialId}/submit-review")
    public MaterialDetailResponse submitReview(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId) {
        return service.submitReview(token(headers), materialId);
    }

    @PostMapping("/materials/{materialId}/review")
    public MaterialDetailResponse review(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId, @RequestBody ReviewRequest request) {
        return service.review(token(headers), materialId, request);
    }

    @PostMapping("/materials/{materialId}/publish")
    public MaterialDetailResponse publish(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @PathVariable UUID materialId, @RequestBody PublishRequest request) {
        return service.publish(token(headers), elevatedSessionId, materialId, request);
    }

    @PostMapping("/materials/{materialId}/archive")
    public ResponseEntity<Void> archive(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId) {
        service.archive(token(headers), materialId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/materials/{materialId}/preview")
    public PreviewResponse preview(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId) {
        return service.preview(token(headers), materialId);
    }

    @GetMapping("/materials/{materialId}/versions")
    public VersionListResponse versions(@RequestHeader HttpHeaders headers, @PathVariable UUID materialId) {
        return service.versions(token(headers), materialId);
    }

    @PostMapping("/materials/{materialId}/versions/{versionId}/rollback")
    public ResponseEntity<MaterialDetailResponse> rollback(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @PathVariable UUID materialId, @PathVariable UUID versionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.rollback(token(headers), elevatedSessionId, materialId, versionId));
    }

    @GetMapping("/audit")
    public AuditResponse audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) UUID materialId, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String correlationId) {
        return service.audit(token(headers), materialId, actionCode, correlationId);
    }

    @ExceptionHandler(AdminCmsAccessDeniedException.class)
    public ResponseEntity<AdminCmsErrorResponse> handleForbidden(AdminCmsAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AdminCmsConflictException.class)
    public ResponseEntity<AdminCmsErrorResponse> handleConflict(AdminCmsConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AdminCmsValidationException.class)
    public ResponseEntity<AdminCmsErrorResponse> handleValidation(AdminCmsValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
    }

    private static AdminCmsErrorResponse error(String messageCode) {
        return new AdminCmsErrorResponse(messageCode, "CORR-027-ERROR");
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
