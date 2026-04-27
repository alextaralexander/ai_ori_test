package com.bestorigin.monolith.adminservice.impl.controller;

import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AdminServiceErrorResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AssignmentRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditEventPage;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditExportRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.AuditExportResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateDecisionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateMessageRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateRefundActionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateReplacementActionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.CreateServiceCaseRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.DecisionResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.RefundActionResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ReplacementActionResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceCaseDetails;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceCasePage;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceMessageResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.ServiceQueueResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.SlaBoardResponse;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.StatusTransitionRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.UpsertQueueRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.WmsEventRequest;
import com.bestorigin.monolith.adminservice.api.AdminServiceDtos.WmsEventResponse;
import com.bestorigin.monolith.adminservice.impl.exception.AdminServiceAccessDeniedException;
import com.bestorigin.monolith.adminservice.impl.exception.AdminServiceConflictException;
import com.bestorigin.monolith.adminservice.impl.exception.AdminServiceValidationException;
import com.bestorigin.monolith.adminservice.impl.service.AdminServiceService;
import java.util.List;
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
@RequestMapping("/api/admin/service")
public class AdminServiceController {

    private final AdminServiceService service;

    public AdminServiceController(AdminServiceService service) {
        this.service = service;
    }

    @GetMapping("/cases")
    public ServiceCasePage cases(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String search, @RequestParam(required = false) String caseStatus, @RequestParam(required = false) String slaStatus, @RequestParam(required = false) String claimType, @RequestParam(required = false) UUID queueId, @RequestParam(required = false) UUID warehouseId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchCases(token(headers), search, caseStatus, slaStatus, claimType, queueId, warehouseId, page, size);
    }

    @PostMapping("/cases")
    public ResponseEntity<ServiceCaseDetails> createCase(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody CreateServiceCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCase(token(headers), idempotencyKey, request));
    }

    @GetMapping("/cases/{serviceCaseId}")
    public ServiceCaseDetails serviceCase(@RequestHeader HttpHeaders headers, @PathVariable UUID serviceCaseId) {
        return service.getCase(token(headers), serviceCaseId);
    }

    @PostMapping("/cases/{serviceCaseId}/assignment")
    public ServiceCaseDetails assign(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID serviceCaseId, @RequestBody AssignmentRequest request) {
        return service.assignCase(token(headers), serviceCaseId, idempotencyKey, request);
    }

    @PostMapping("/cases/{serviceCaseId}/status-transition")
    public ServiceCaseDetails transition(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID serviceCaseId, @RequestBody StatusTransitionRequest request) {
        return service.transitionStatus(token(headers), serviceCaseId, idempotencyKey, request);
    }

    @PostMapping("/cases/{serviceCaseId}/messages")
    public ResponseEntity<ServiceMessageResponse> message(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID serviceCaseId, @RequestBody CreateMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMessage(token(headers), serviceCaseId, idempotencyKey, request));
    }

    @GetMapping("/queues")
    public List<ServiceQueueResponse> queues(@RequestHeader HttpHeaders headers) {
        return service.queues(token(headers));
    }

    @PostMapping("/queues")
    public ServiceQueueResponse queue(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody UpsertQueueRequest request) {
        return service.upsertQueue(token(headers), idempotencyKey, request);
    }

    @PostMapping("/cases/{serviceCaseId}/decisions")
    public ResponseEntity<DecisionResponse> decision(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID serviceCaseId, @RequestBody CreateDecisionRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.createDecision(token(headers), serviceCaseId, idempotencyKey, request));
    }

    @PostMapping("/decisions/{decisionId}/refund-actions")
    public ResponseEntity<RefundActionResponse> refund(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID decisionId, @RequestBody CreateRefundActionRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.createRefundAction(token(headers), decisionId, idempotencyKey, request));
    }

    @PostMapping("/decisions/{decisionId}/replacement-actions")
    public ResponseEntity<ReplacementActionResponse> replacement(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID decisionId, @RequestBody CreateReplacementActionRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.createReplacementAction(token(headers), decisionId, idempotencyKey, request));
    }

    @PostMapping("/wms-events")
    public ResponseEntity<WmsEventResponse> wmsEvent(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody WmsEventRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.ingestWmsEvent(token(headers), idempotencyKey, request));
    }

    @GetMapping("/supervisor/sla-board")
    public SlaBoardResponse slaBoard(@RequestHeader HttpHeaders headers) {
        return service.slaBoard(token(headers));
    }

    @GetMapping("/audit-events")
    public AuditEventPage audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String entityType, @RequestParam(required = false) String entityId, @RequestParam(required = false) UUID actorUserId, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.audit(token(headers), entityType, entityId, actorUserId, correlationId, page, size);
    }

    @PostMapping("/audit-events/export")
    public ResponseEntity<AuditExportResponse> exportAudit(@RequestHeader HttpHeaders headers, @RequestBody AuditExportRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.exportAudit(token(headers), request));
    }

    @ExceptionHandler(AdminServiceAccessDeniedException.class)
    public ResponseEntity<AdminServiceErrorResponse> handleForbidden(AdminServiceAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminServiceConflictException.class)
    public ResponseEntity<AdminServiceErrorResponse> handleConflict(AdminServiceConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminServiceValidationException.class)
    public ResponseEntity<AdminServiceErrorResponse> handleValidation(AdminServiceValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminServiceErrorResponse error(String messageCode, List<String> details) {
        return new AdminServiceErrorResponse(messageCode, "CORR-034-ERROR", details == null ? List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
