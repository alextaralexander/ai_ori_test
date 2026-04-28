package com.bestorigin.monolith.adminbenefitprogram.impl.controller;

import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.AdminBenefitProgramErrorResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.AuditEventPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BudgetRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BudgetResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.DryRunRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.DryRunResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ManualAdjustmentRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ManualAdjustmentResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ProgramStatusRequest;
import com.bestorigin.monolith.adminbenefitprogram.impl.exception.AdminBenefitProgramAccessDeniedException;
import com.bestorigin.monolith.adminbenefitprogram.impl.exception.AdminBenefitProgramConflictException;
import com.bestorigin.monolith.adminbenefitprogram.impl.exception.AdminBenefitProgramValidationException;
import com.bestorigin.monolith.adminbenefitprogram.impl.service.AdminBenefitProgramService;
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
@RequestMapping("/api/admin/benefit-programs")
public class AdminBenefitProgramController {
    private final AdminBenefitProgramService service;

    public AdminBenefitProgramController(AdminBenefitProgramService service) {
        this.service = service;
    }

    @GetMapping("/programs")
    public BenefitProgramPage searchPrograms(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String catalogId, @RequestParam(required = false) String type) {
        return service.searchPrograms(token(headers), status, catalogId, type);
    }

    @PostMapping("/programs")
    public ResponseEntity<BenefitProgramResponse> createProgram(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody BenefitProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProgram(token(headers), idempotencyKey, request));
    }

    @PostMapping("/programs/{programId}/dry-run")
    public DryRunResponse dryRun(@RequestHeader HttpHeaders headers, @PathVariable UUID programId, @RequestBody DryRunRequest request) {
        return service.dryRun(token(headers), programId, request);
    }

    @PostMapping("/programs/{programId}/status")
    public BenefitProgramResponse changeStatus(@RequestHeader HttpHeaders headers, @PathVariable UUID programId, @RequestBody ProgramStatusRequest request) {
        return service.changeStatus(token(headers), programId, request);
    }

    @PutMapping("/programs/{programId}/budgets")
    public BudgetResponse updateBudget(@RequestHeader HttpHeaders headers, @PathVariable UUID programId, @RequestBody BudgetRequest request) {
        return service.updateBudget(token(headers), programId, request);
    }

    @PostMapping("/programs/{programId}/manual-adjustments")
    public ResponseEntity<ManualAdjustmentResponse> createManualAdjustment(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID programId, @RequestBody ManualAdjustmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createManualAdjustment(token(headers), idempotencyKey, programId, request));
    }

    @GetMapping("/programs/{programId}/audit-events")
    public AuditEventPage auditEvents(@RequestHeader HttpHeaders headers, @PathVariable UUID programId, @RequestParam(required = false) String actionCode) {
        return service.auditEvents(token(headers), programId, actionCode);
    }

    @GetMapping("/programs/{programId}/integration-events")
    public IntegrationEventPage integrationEvents(@RequestHeader HttpHeaders headers, @PathVariable UUID programId, @RequestParam(required = false) String targetContext) {
        return service.integrationEvents(token(headers), programId, targetContext);
    }

    @ExceptionHandler(AdminBenefitProgramAccessDeniedException.class)
    public ResponseEntity<AdminBenefitProgramErrorResponse> handleForbidden(AdminBenefitProgramAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminBenefitProgramConflictException.class)
    public ResponseEntity<AdminBenefitProgramErrorResponse> handleConflict(AdminBenefitProgramConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AdminBenefitProgramValidationException.class)
    public ResponseEntity<AdminBenefitProgramErrorResponse> handleValidation(AdminBenefitProgramValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminBenefitProgramErrorResponse error(String messageCode, List<String> details) {
        return new AdminBenefitProgramErrorResponse(messageCode, "CORR-041-ERROR", details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return value == null ? "" : value.replace("Bearer ", "").trim();
    }
}
