package com.bestorigin.monolith.adminorder.impl.controller;

import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderDetails;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderErrorResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AdminOrderPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditEventPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditExportRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.AuditExportResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateFinancialHoldRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateRefundRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.CreateSupplementaryOrderRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.FinancialHoldResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.OperatorActionRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventPage;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.PaymentEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RefundResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.ReleaseFinancialHoldRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RiskDecisionRequest;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.RiskEventResponse;
import com.bestorigin.monolith.adminorder.api.AdminOrderDtos.StatusTransitionRequest;
import com.bestorigin.monolith.adminorder.impl.exception.AdminOrderAccessDeniedException;
import com.bestorigin.monolith.adminorder.impl.exception.AdminOrderConflictException;
import com.bestorigin.monolith.adminorder.impl.exception.AdminOrderValidationException;
import com.bestorigin.monolith.adminorder.impl.service.AdminOrderService;
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
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService service;

    public AdminOrderController(AdminOrderService service) {
        this.service = service;
    }

    @GetMapping("/orders")
    public AdminOrderPage orders(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String search, @RequestParam(required = false) String orderStatus, @RequestParam(required = false) String paymentStatus, @RequestParam(required = false) String fulfillmentStatus, @RequestParam(required = false) UUID warehouseId, @RequestParam(required = false) String catalogPeriodCode, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchOrders(token(headers), search, orderStatus, paymentStatus, fulfillmentStatus, warehouseId, catalogPeriodCode, page, size);
    }

    @GetMapping("/orders/{orderId}")
    public AdminOrderDetails order(@RequestHeader HttpHeaders headers, @PathVariable UUID orderId) {
        return service.getOrder(token(headers), orderId);
    }

    @PostMapping("/orders/{orderId}/status-transition")
    public AdminOrderDetails statusTransition(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID orderId, @RequestBody StatusTransitionRequest request) {
        return service.transitionStatus(token(headers), orderId, idempotencyKey, request);
    }

    @PostMapping("/orders/{orderId}/supplementary-orders")
    public ResponseEntity<AdminOrderDetails> supplementaryOrder(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID orderId, @RequestBody CreateSupplementaryOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSupplementaryOrder(token(headers), orderId, idempotencyKey, request));
    }

    @PostMapping("/orders/{orderId}/operator-actions")
    public AdminOrderDetails operatorAction(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID orderId, @RequestBody OperatorActionRequest request) {
        return service.executeOperatorAction(token(headers), orderId, idempotencyKey, request);
    }

    @PostMapping("/payment-events")
    public ResponseEntity<PaymentEventResponse> paymentEvent(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody PaymentEventRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.ingestPaymentEvent(token(headers), idempotencyKey, request));
    }

    @GetMapping("/payment-events")
    public PaymentEventPage paymentEvents(@RequestHeader HttpHeaders headers, @RequestParam(required = false) UUID orderId, @RequestParam(required = false) String externalPaymentId, @RequestParam(required = false) String operationType, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.searchPaymentEvents(token(headers), orderId, externalPaymentId, operationType, page, size);
    }

    @PostMapping("/orders/{orderId}/refunds")
    public ResponseEntity<RefundResponse> refund(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID orderId, @RequestBody CreateRefundRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.createRefund(token(headers), orderId, idempotencyKey, request));
    }

    @PostMapping("/orders/{orderId}/financial-holds")
    public ResponseEntity<FinancialHoldResponse> financialHold(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID orderId, @RequestBody CreateFinancialHoldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFinancialHold(token(headers), orderId, idempotencyKey, request));
    }

    @PostMapping("/financial-holds/{financialHoldId}/release")
    public FinancialHoldResponse releaseFinancialHold(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID financialHoldId, @RequestBody ReleaseFinancialHoldRequest request) {
        return service.releaseFinancialHold(token(headers), financialHoldId, idempotencyKey, request);
    }

    @PostMapping("/orders/{orderId}/risk-decisions")
    public RiskEventResponse riskDecision(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable UUID orderId, @RequestBody RiskDecisionRequest request) {
        return service.decideRisk(token(headers), orderId, idempotencyKey, request);
    }

    @GetMapping("/audit-events")
    public AuditEventPage audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String entityType, @RequestParam(required = false) String entityId, @RequestParam(required = false) UUID actorUserId, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.audit(token(headers), entityType, entityId, actorUserId, correlationId, page, size);
    }

    @PostMapping("/audit-events/export")
    public ResponseEntity<AuditExportResponse> exportAudit(@RequestHeader HttpHeaders headers, @RequestBody AuditExportRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.exportAudit(token(headers), request));
    }

    @ExceptionHandler(AdminOrderAccessDeniedException.class)
    public ResponseEntity<AdminOrderErrorResponse> handleForbidden(AdminOrderAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminOrderConflictException.class)
    public ResponseEntity<AdminOrderErrorResponse> handleConflict(AdminOrderConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminOrderValidationException.class)
    public ResponseEntity<AdminOrderErrorResponse> handleValidation(AdminOrderValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminOrderErrorResponse error(String messageCode, java.util.List<String> details) {
        return new AdminOrderErrorResponse(messageCode, "CORR-033-ERROR", details == null ? java.util.List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
