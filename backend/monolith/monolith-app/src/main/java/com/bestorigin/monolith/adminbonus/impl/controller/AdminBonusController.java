package com.bestorigin.monolith.adminbonus.impl.controller;

import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.AccrualPage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.AdminBonusErrorResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusPreviewRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusPreviewResult;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRulePage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRuleRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.BonusRuleResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.CalculationRunRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.CalculationRunResult;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.PayoutBatchRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.PayoutBatchResponse;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.QualificationRequest;
import com.bestorigin.monolith.adminbonus.api.AdminBonusDtos.QualificationResponse;
import com.bestorigin.monolith.adminbonus.impl.exception.AdminBonusAccessDeniedException;
import com.bestorigin.monolith.adminbonus.impl.exception.AdminBonusConflictException;
import com.bestorigin.monolith.adminbonus.impl.exception.AdminBonusValidationException;
import com.bestorigin.monolith.adminbonus.impl.service.AdminBonusService;
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
@RequestMapping("/api/admin/bonus-program")
public class AdminBonusController {
    private final AdminBonusService service;

    public AdminBonusController(AdminBonusService service) {
        this.service = service;
    }

    @GetMapping("/rules")
    public BonusRulePage searchRules(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String ruleType) {
        return service.searchRules(token(headers), status, ruleType);
    }

    @PostMapping("/rules")
    public ResponseEntity<BonusRuleResponse> createRule(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody BonusRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRule(token(headers), idempotencyKey, request));
    }

    @PostMapping("/rules/{ruleId}/preview")
    public BonusPreviewResult previewRule(@RequestHeader HttpHeaders headers, @PathVariable UUID ruleId, @RequestBody BonusPreviewRequest request) {
        return service.previewRule(token(headers), ruleId, request);
    }

    @PostMapping("/rules/{ruleId}/activate")
    public BonusRuleResponse activateRule(@RequestHeader HttpHeaders headers, @PathVariable UUID ruleId) {
        return service.activateRule(token(headers), ruleId);
    }

    @PostMapping("/qualifications")
    public ResponseEntity<QualificationResponse> createQualification(@RequestHeader HttpHeaders headers, @RequestBody QualificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQualification(token(headers), request));
    }

    @PostMapping("/calculations")
    public ResponseEntity<CalculationRunResult> runCalculation(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody CalculationRunRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.runCalculation(token(headers), idempotencyKey, request));
    }

    @GetMapping("/accruals")
    public AccrualPage accruals(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String periodCode, @RequestParam(required = false) String partnerId, @RequestParam(required = false) String status) {
        return service.searchAccruals(token(headers), periodCode, partnerId, status);
    }

    @PostMapping("/payout-batches")
    public ResponseEntity<PayoutBatchResponse> createPayoutBatch(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody PayoutBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayoutBatch(token(headers), idempotencyKey, request));
    }

    @PostMapping("/payout-batches/{batchId}/approve")
    public PayoutBatchResponse approvePayoutBatch(@RequestHeader HttpHeaders headers, @PathVariable UUID batchId) {
        return service.approvePayoutBatch(token(headers), batchId);
    }

    @PostMapping("/payout-batches/{batchId}/send")
    public ResponseEntity<PayoutBatchResponse> sendPayoutBatch(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID batchId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.sendPayoutBatch(token(headers), batchId, idempotencyKey));
    }

    @GetMapping("/integrations/events")
    public IntegrationEventPage integrationEvents(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String correlationId) {
        return service.integrationEvents(token(headers), correlationId);
    }

    @ExceptionHandler(AdminBonusAccessDeniedException.class)
    public ResponseEntity<AdminBonusErrorResponse> handleForbidden(AdminBonusAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminBonusConflictException.class)
    public ResponseEntity<AdminBonusErrorResponse> handleConflict(AdminBonusConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminBonusValidationException.class)
    public ResponseEntity<AdminBonusErrorResponse> handleValidation(AdminBonusValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminBonusErrorResponse error(String messageCode, List<String> details) {
        return new AdminBonusErrorResponse(messageCode, "CORR-038-ERROR", details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return value == null ? "" : value.replace("Bearer ", "").trim();
    }
}
