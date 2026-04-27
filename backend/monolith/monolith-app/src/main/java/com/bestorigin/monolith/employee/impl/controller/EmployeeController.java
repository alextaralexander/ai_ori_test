package com.bestorigin.monolith.employee.impl.controller;

import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeConfirmOrderRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimTransitionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAddressResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAddressUpsertRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAddressesResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeContactResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeContactUpsertRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeContactsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeDocumentCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeDocumentResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeDocumentsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeErrorResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedDecisionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedRequestCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedRequestResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedSessionResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeEscalationPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryFilterRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderSupportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerCardResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerOrderReportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeProfileGeneralResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeProfileGeneralUpdateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeProfileSettingsSummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSecuritySummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSuperUserDashboardResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping("/submit-claim")
    public ResponseEntity<EmployeeClaimDetailsResponse> submitClaim(@RequestHeader HttpHeaders headers, @RequestBody EmployeeClaimCreateRequest request) {
        EmployeeClaimDetailsResponse response = service.submitClaim(userContext(headers), request, idempotencyKey(headers));
        HttpStatus status = idempotencyKey(headers).contains("IDEMPOTENT") ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/claims")
    public EmployeeClaimPageResponse claims(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String claimStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String slaState,
            @RequestParam(required = false) String responsibleRole,
            @RequestParam(required = false) String assigneeId,
            @RequestParam(required = false) String resolutionType,
            @RequestParam(required = false) String sourceChannel,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String financeStatus,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "slaDueAt,asc") String sort) {
        return service.claims(userContext(headers), claimStatus, dateFrom, dateTo, slaState, responsibleRole, assigneeId, resolutionType, sourceChannel, warehouseCode, financeStatus, query, page, size, sort);
    }

    @GetMapping("/claims/{claimId}")
    public EmployeeClaimDetailsResponse claimDetails(@RequestHeader HttpHeaders headers, @PathVariable String claimId, @RequestParam(required = false) String supportReasonCode) {
        return service.claimDetails(userContext(headers), claimId, supportReasonCode == null ? "EMPLOYEE_CLAIM_VIEW" : supportReasonCode);
    }

    @PostMapping("/claims/{claimId}/transitions")
    public EmployeeClaimDetailsResponse transitionClaim(@RequestHeader HttpHeaders headers, @PathVariable String claimId, @RequestBody EmployeeClaimTransitionRequest request) {
        return service.transitionClaim(userContext(headers), claimId, request, idempotencyKey(headers));
    }

    @GetMapping("/partner-card")
    public EmployeePartnerCardResponse partnerCard(
            @RequestHeader HttpHeaders headers,
            @RequestParam String query,
            @RequestParam(required = false) String supportReasonCode,
            @RequestParam(required = false) String regionCode) {
        return service.partnerCard(userContext(headers), query, supportReasonCode == null ? "EMPLOYEE_PARTNER_CARD_VIEW" : supportReasonCode, regionCode);
    }

    @GetMapping("/partner-card/{partnerId}")
    public EmployeePartnerCardResponse partnerCardById(
            @RequestHeader HttpHeaders headers,
            @PathVariable String partnerId,
            @RequestParam(required = false) String supportReasonCode) {
        return service.partnerCardById(userContext(headers), partnerId, supportReasonCode == null ? "EMPLOYEE_PARTNER_CARD_VIEW" : supportReasonCode);
    }

    @GetMapping("/report/order-history")
    public EmployeePartnerOrderReportResponse partnerOrderReport(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String partnerId,
            @RequestParam(required = false) String personNumber,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String campaignCode,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String deliveryStatus,
            @RequestParam(defaultValue = "false") boolean problemOnly,
            @RequestParam(required = false) String regionCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        return service.partnerOrderReport(userContext(headers), partnerId, personNumber, dateFrom, dateTo, campaignCode, orderStatus, paymentStatus, deliveryStatus, problemOnly, regionCode, page, size, sort);
    }

    @GetMapping("/profile-settings")
    public EmployeeProfileSettingsSummaryResponse profileSettings(@RequestHeader HttpHeaders headers) {
        return service.profileSettings(userContext(headers));
    }

    @GetMapping("/profile-settings/general")
    public EmployeeProfileGeneralResponse profileGeneral(@RequestHeader HttpHeaders headers) {
        return service.profileGeneral(userContext(headers));
    }

    @PutMapping("/profile-settings/general")
    public EmployeeProfileGeneralResponse updateProfileGeneral(@RequestHeader HttpHeaders headers, @RequestBody EmployeeProfileGeneralUpdateRequest request) {
        return service.updateProfileGeneral(userContext(headers), request);
    }

    @GetMapping("/profile-settings/contacts")
    public EmployeeContactsResponse contacts(@RequestHeader HttpHeaders headers) {
        return service.contacts(userContext(headers));
    }

    @PostMapping("/profile-settings/contacts")
    public ResponseEntity<EmployeeContactResponse> createContact(@RequestHeader HttpHeaders headers, @RequestBody EmployeeContactUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createContact(userContext(headers), request));
    }

    @GetMapping("/profile-settings/addresses")
    public EmployeeAddressesResponse addresses(@RequestHeader HttpHeaders headers) {
        return service.addresses(userContext(headers));
    }

    @PostMapping("/profile-settings/addresses")
    public ResponseEntity<EmployeeAddressResponse> createAddress(@RequestHeader HttpHeaders headers, @RequestBody EmployeeAddressUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAddress(userContext(headers), request));
    }

    @GetMapping("/profile-settings/documents")
    public EmployeeDocumentsResponse documents(@RequestHeader HttpHeaders headers) {
        return service.documents(userContext(headers));
    }

    @PostMapping("/profile-settings/documents")
    public ResponseEntity<EmployeeDocumentResponse> createDocument(@RequestHeader HttpHeaders headers, @RequestBody EmployeeDocumentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDocument(userContext(headers), request));
    }

    @GetMapping("/profile-settings/security")
    public EmployeeSecuritySummaryResponse security(@RequestHeader HttpHeaders headers) {
        return service.security(userContext(headers));
    }

    @GetMapping("/super-user")
    public EmployeeSuperUserDashboardResponse superUser(@RequestHeader HttpHeaders headers) {
        return service.superUser(userContext(headers));
    }

    @PostMapping("/super-user/requests")
    public ResponseEntity<EmployeeElevatedRequestResponse> createElevatedRequest(@RequestHeader HttpHeaders headers, @RequestBody EmployeeElevatedRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createElevatedRequest(userContext(headers), request));
    }

    @PostMapping("/super-user/requests/{requestId}/approve")
    public EmployeeElevatedSessionResponse approveElevatedRequest(@RequestHeader HttpHeaders headers, @PathVariable UUID requestId, @RequestBody(required = false) EmployeeElevatedDecisionRequest request) {
        return service.approveElevatedRequest(userContext(headers), requestId, request);
    }

    @PostMapping("/super-user/requests/{requestId}/reject")
    public EmployeeElevatedRequestResponse rejectElevatedRequest(@RequestHeader HttpHeaders headers, @PathVariable UUID requestId, @RequestBody(required = false) EmployeeElevatedDecisionRequest request) {
        return service.rejectElevatedRequest(userContext(headers), requestId, request);
    }

    @PostMapping("/super-user/sessions/{sessionId}/close")
    public ResponseEntity<Void> closeElevatedSession(@RequestHeader HttpHeaders headers, @PathVariable UUID sessionId, @RequestBody(required = false) EmployeeElevatedDecisionRequest request) {
        service.closeElevatedSession(userContext(headers), sessionId, request);
        return ResponseEntity.noContent().build();
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
