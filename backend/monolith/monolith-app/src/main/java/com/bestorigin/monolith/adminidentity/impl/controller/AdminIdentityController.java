package com.bestorigin.monolith.adminidentity.impl.controller;

import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.AdminIdentityErrorResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.AuditEventPage;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ChangeStatusRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRulesResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EligibilityRulesUpdateRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingsResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.EmployeeBindingsUpdateRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationPolicy;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationPolicySaveRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ImpersonationSession;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.ReasonedActionRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SponsorRelationshipChangeRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SponsorRelationshipResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.StartImpersonationRequest;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.StatusChangeResponse;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectCard;
import com.bestorigin.monolith.adminidentity.api.AdminIdentityDtos.SubjectSearchResponse;
import com.bestorigin.monolith.adminidentity.impl.exception.AdminIdentityAccessDeniedException;
import com.bestorigin.monolith.adminidentity.impl.exception.AdminIdentityConflictException;
import com.bestorigin.monolith.adminidentity.impl.exception.AdminIdentityValidationException;
import com.bestorigin.monolith.adminidentity.impl.service.AdminIdentityService;
import java.util.List;
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
@RequestMapping("/api/admin/identity")
public class AdminIdentityController {

    private final AdminIdentityService service;

    public AdminIdentityController(AdminIdentityService service) {
        this.service = service;
    }

    @GetMapping("/subjects")
    public SubjectSearchResponse subjects(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String query, @RequestParam(required = false) String subjectType, @RequestParam(required = false) String status, @RequestParam(required = false) String sponsorCode, @RequestParam(required = false) String officeId, @RequestParam(required = false) String employeeRole, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchSubjects(token(headers), query, subjectType, status, sponsorCode, officeId, employeeRole, page, size);
    }

    @GetMapping("/subjects/{subjectId}")
    public SubjectCard subjectCard(@RequestHeader HttpHeaders headers, @PathVariable UUID subjectId) {
        return service.getSubjectCard(token(headers), subjectId);
    }

    @PostMapping("/subjects/{subjectId}/status")
    public StatusChangeResponse status(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID subjectId, @RequestBody ChangeStatusRequest request) {
        return service.changeStatus(token(headers), subjectId, idempotencyKey, request);
    }

    @PutMapping("/subjects/{subjectId}/eligibility-rules")
    public EligibilityRulesResponse eligibilityRules(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID subjectId, @RequestBody EligibilityRulesUpdateRequest request) {
        return service.updateEligibilityRules(token(headers), subjectId, idempotencyKey, request);
    }

    @PostMapping("/partners/{partnerSubjectId}/sponsor-relationships")
    public SponsorRelationshipResponse sponsorRelationship(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID partnerSubjectId, @RequestBody SponsorRelationshipChangeRequest request) {
        return service.changeSponsor(token(headers), partnerSubjectId, idempotencyKey, request);
    }

    @PutMapping("/employees/{employeeSubjectId}/bindings")
    public EmployeeBindingsResponse employeeBindings(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID employeeSubjectId, @RequestBody EmployeeBindingsUpdateRequest request) {
        return service.updateEmployeeBindings(token(headers), employeeSubjectId, idempotencyKey, request);
    }

    @GetMapping("/impersonation/policies")
    public List<ImpersonationPolicy> policies(@RequestHeader HttpHeaders headers) {
        return service.policies(token(headers));
    }

    @PostMapping("/impersonation/policies")
    public ImpersonationPolicy savePolicy(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ImpersonationPolicySaveRequest request) {
        return service.savePolicy(token(headers), idempotencyKey, request);
    }

    @PostMapping("/impersonation/sessions")
    public ResponseEntity<ImpersonationSession> startSession(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody StartImpersonationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.startSession(token(headers), idempotencyKey, request));
    }

    @PostMapping("/impersonation/sessions/{sessionId}/finish")
    public ImpersonationSession finishSession(@RequestHeader HttpHeaders headers, @PathVariable UUID sessionId, @RequestBody ReasonedActionRequest request) {
        return service.finishSession(token(headers), sessionId, request);
    }

    @GetMapping("/audit-events")
    public AuditEventPage auditEvents(@RequestHeader HttpHeaders headers, @RequestParam(required = false) UUID subjectId, @RequestParam(required = false) UUID actorSubjectId, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String reasonCode, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.auditEvents(token(headers), subjectId, actorSubjectId, actionCode, reasonCode, page, size);
    }

    @ExceptionHandler(AdminIdentityAccessDeniedException.class)
    public ResponseEntity<AdminIdentityErrorResponse> handleForbidden(AdminIdentityAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminIdentityConflictException.class)
    public ResponseEntity<AdminIdentityErrorResponse> handleConflict(AdminIdentityConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminIdentityValidationException.class)
    public ResponseEntity<AdminIdentityErrorResponse> handleValidation(AdminIdentityValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminIdentityErrorResponse error(String messageCode, List<String> details) {
        return new AdminIdentityErrorResponse(messageCode, "CORR-035-ERROR", details == null ? List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
