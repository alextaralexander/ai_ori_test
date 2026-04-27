package com.bestorigin.monolith.adminrbac.impl.controller;

import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AccountAccessUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AdminRbacErrorResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.AuditEventListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.EffectivePermissionPreviewResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.EmergencyDeactivationRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountCreateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.InternalAccountUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.RoleCatalogResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.SecurityPolicyUpdateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountCreateRequest;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountListResponse;
import com.bestorigin.monolith.adminrbac.api.AdminRbacDtos.ServiceAccountSecretResponse;
import com.bestorigin.monolith.adminrbac.impl.exception.AdminRbacAccessDeniedException;
import com.bestorigin.monolith.adminrbac.impl.exception.AdminRbacConflictException;
import com.bestorigin.monolith.adminrbac.impl.exception.AdminRbacValidationException;
import com.bestorigin.monolith.adminrbac.impl.service.AdminRbacService;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rbac")
public class AdminRbacController {

    private final AdminRbacService service;

    public AdminRbacController(AdminRbacService service) {
        this.service = service;
    }

    @GetMapping("/accounts")
    public InternalAccountListResponse accounts(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String query, @RequestParam(required = false) String status, @RequestParam(required = false) String department) {
        return service.searchAccounts(token(headers), query, status, department);
    }

    @PostMapping("/accounts")
    public ResponseEntity<InternalAccountResponse> createAccount(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @RequestBody InternalAccountCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(token(headers), elevatedSessionId, request));
    }

    @GetMapping("/accounts/{accountId}")
    public InternalAccountResponse account(@RequestHeader HttpHeaders headers, @PathVariable UUID accountId) {
        return service.getAccount(token(headers), accountId);
    }

    @PatchMapping("/accounts/{accountId}")
    public InternalAccountResponse updateAccount(@RequestHeader HttpHeaders headers, @PathVariable UUID accountId, @RequestBody InternalAccountUpdateRequest request) {
        return service.updateAccount(token(headers), accountId, request);
    }

    @PutMapping("/accounts/{accountId}/roles")
    public EffectivePermissionPreviewResponse updateAccess(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @PathVariable UUID accountId, @RequestBody AccountAccessUpdateRequest request) {
        return service.updateAccess(token(headers), elevatedSessionId, accountId, request);
    }

    @PostMapping("/accounts/{accountId}/permission-preview")
    public EffectivePermissionPreviewResponse permissionPreview(@RequestHeader HttpHeaders headers, @PathVariable UUID accountId, @RequestBody AccountAccessUpdateRequest request) {
        return service.previewAccess(token(headers), accountId, request);
    }

    @PostMapping("/accounts/{accountId}/block")
    public ResponseEntity<Void> block(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @PathVariable UUID accountId) {
        service.blockAccount(token(headers), elevatedSessionId, accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/{accountId}/deactivate")
    public ResponseEntity<Void> deactivate(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @PathVariable UUID accountId) {
        service.deactivateAccount(token(headers), elevatedSessionId, accountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public RoleCatalogResponse roles(@RequestHeader HttpHeaders headers) {
        return service.roleCatalog(token(headers));
    }

    @GetMapping("/security-policies")
    public SecurityPolicyListResponse securityPolicies(@RequestHeader HttpHeaders headers) {
        return service.securityPolicies(token(headers));
    }

    @PutMapping("/security-policies")
    public SecurityPolicyResponse updatePolicy(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @RequestBody SecurityPolicyUpdateRequest request) {
        return service.updateSecurityPolicy(token(headers), elevatedSessionId, request);
    }

    @GetMapping("/service-accounts")
    public ServiceAccountListResponse serviceAccounts(@RequestHeader HttpHeaders headers) {
        return service.serviceAccounts(token(headers));
    }

    @PostMapping("/service-accounts")
    public ResponseEntity<ServiceAccountSecretResponse> createServiceAccount(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @RequestBody ServiceAccountCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createServiceAccount(token(headers), elevatedSessionId, request));
    }

    @PostMapping("/service-accounts/{serviceAccountId}/rotate-secret")
    public ServiceAccountSecretResponse rotateServiceAccount(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @PathVariable UUID serviceAccountId) {
        return service.rotateServiceAccount(token(headers), elevatedSessionId, serviceAccountId);
    }

    @PostMapping("/emergency-deactivations")
    public ResponseEntity<Void> emergencyDeactivate(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @RequestBody EmergencyDeactivationRequest request) {
        service.emergencyDeactivate(token(headers), elevatedSessionId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-events")
    public AuditEventListResponse auditEvents(@RequestHeader HttpHeaders headers, @RequestParam(required = false) UUID actorUserId, @RequestParam(required = false) UUID targetUserId, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String dateFrom, @RequestParam(required = false) String dateTo, @RequestParam(required = false) String correlationId) {
        return service.auditEvents(token(headers), actorUserId, targetUserId, actionCode, dateFrom, dateTo, correlationId);
    }

    @ExceptionHandler(AdminRbacAccessDeniedException.class)
    public ResponseEntity<AdminRbacErrorResponse> handleForbidden(AdminRbacAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AdminRbacConflictException.class)
    public ResponseEntity<AdminRbacErrorResponse> handleConflict(AdminRbacConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AdminRbacValidationException.class)
    public ResponseEntity<AdminRbacErrorResponse> handleValidation(AdminRbacValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
    }

    private static AdminRbacErrorResponse error(String code) {
        return new AdminRbacErrorResponse(code, "CORR-026-ERROR");
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
