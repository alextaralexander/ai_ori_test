package com.bestorigin.monolith.employee.impl.controller;

import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeConfirmOrderRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeErrorResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeEscalationPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryFilterRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderSupportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSupportActionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSupportActionResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeWarningResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeWorkspaceResponse;
import com.bestorigin.monolith.employee.impl.exception.EmployeeAccessDeniedException;
import com.bestorigin.monolith.employee.impl.exception.EmployeeNotFoundException;
import com.bestorigin.monolith.employee.impl.exception.EmployeeValidationException;
import com.bestorigin.monolith.employee.impl.service.EmployeeService;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping("/workspace")
    public EmployeeWorkspaceResponse workspace(@RequestHeader HttpHeaders headers, @RequestParam String query) {
        return service.workspace(userContext(headers), query);
    }

    @PostMapping("/operator-orders")
    public EmployeeOperatorOrderResponse createOperatorOrder(@RequestHeader HttpHeaders headers, @RequestBody EmployeeOperatorOrderCreateRequest request) {
        return service.createOperatorOrder(userContext(headers), request, idempotencyKey(headers));
    }

    @PostMapping("/operator-orders/{operatorOrderId}/confirm")
    public EmployeeOperatorOrderResponse confirmOperatorOrder(@RequestHeader HttpHeaders headers, @PathVariable UUID operatorOrderId, @RequestBody EmployeeConfirmOrderRequest request) {
        return service.confirmOperatorOrder(userContext(headers), operatorOrderId, request, idempotencyKey(headers));
    }

    @GetMapping("/order-support/{orderNumber}")
    public EmployeeOrderSupportResponse orderSupport(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber) {
        return service.orderSupport(userContext(headers), orderNumber);
    }

    @PostMapping("/order-support/{orderNumber}/notes")
    public EmployeeSupportActionResponse addNote(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber, @RequestBody EmployeeSupportActionRequest request) {
        return service.addInternalNote(userContext(headers), orderNumber, request, idempotencyKey(headers));
    }

    @PostMapping("/order-support/{orderNumber}/adjustments")
    public EmployeeSupportActionResponse adjustment(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber, @RequestBody EmployeeSupportActionRequest request) {
        return service.recordAdjustment(userContext(headers), orderNumber, request, idempotencyKey(headers));
    }

    @PostMapping("/order-support/{orderNumber}/escalations")
    public EmployeeSupportActionResponse escalation(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber, @RequestBody EmployeeSupportActionRequest request) {
        return service.createEscalation(userContext(headers), orderNumber, request, idempotencyKey(headers));
    }

    @GetMapping("/supervisor/escalations")
    public EmployeeEscalationPageResponse supervisorEscalations(@RequestHeader HttpHeaders headers, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.supervisorEscalations(userContext(headers), page, size);
    }

    @GetMapping("/order-history")
    public EmployeeOrderHistoryPageResponse orderHistory(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String partnerId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) com.bestorigin.monolith.employee.api.EmployeeDtos.PaymentStatus paymentStatus,
            @RequestParam(required = false) com.bestorigin.monolith.employee.api.EmployeeDtos.DeliveryStatus deliveryStatus,
            @RequestParam(defaultValue = "false") boolean problemOnly,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        return service.orderHistory(userContext(headers), new EmployeeOrderHistoryFilterRequest(partnerId, customerId, dateFrom, dateTo, orderStatus, paymentStatus, deliveryStatus, problemOnly, query, page, size, sort));
    }

    @GetMapping("/order-history/{orderId}")
    public EmployeeOrderHistoryDetailsResponse orderHistoryDetails(@RequestHeader HttpHeaders headers, @PathVariable String orderId) {
        return service.orderHistoryDetails(userContext(headers), orderId);
    }

    @ExceptionHandler(EmployeeAccessDeniedException.class)
    public ResponseEntity<EmployeeErrorResponse> handleForbidden(EmployeeAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<EmployeeErrorResponse> handleNotFound(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
    }

    @ExceptionHandler(EmployeeValidationException.class)
    public ResponseEntity<EmployeeErrorResponse> handleValidation(EmployeeValidationException ex) {
        return ResponseEntity.status(ex.statusCode()).body(error(ex.getMessage()));
    }

    private static EmployeeErrorResponse error(String code) {
        return new EmployeeErrorResponse(code, List.of(new EmployeeWarningResponse(code, "BLOCKING", "employee")), Map.of());
    }

    private static String userContext(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        return value.replace("Bearer ", "").trim();
    }

    private static String idempotencyKey(HttpHeaders headers) {
        String value = headers.getFirst("Idempotency-Key");
        return value == null || value.isBlank() ? "implicit-idempotency-key" : value;
    }
}
