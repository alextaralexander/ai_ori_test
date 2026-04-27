package com.bestorigin.monolith.partneroffice.impl.controller;

import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeActionResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeDeviationCreateRequest;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeErrorResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeOrderPageResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeReportResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyDetailsResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyOrderDetailsResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyPageResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyTransitionRequest;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeValidationReasonResponse;
import com.bestorigin.monolith.partneroffice.impl.exception.PartnerOfficeAccessDeniedException;
import com.bestorigin.monolith.partneroffice.impl.exception.PartnerOfficeNotFoundException;
import com.bestorigin.monolith.partneroffice.impl.exception.PartnerOfficeValidationException;
import com.bestorigin.monolith.partneroffice.impl.service.PartnerOfficeService;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/partner-office")
public class PartnerOfficeController {

    private final PartnerOfficeService service;

    public PartnerOfficeController(PartnerOfficeService service) {
        this.service = service;
    }

    @GetMapping("/orders")
    public PartnerOfficeOrderPageResponse orders(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String supplyId,
            @RequestParam(required = false) Boolean hasDeviation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.searchOrders(userContext(headers), campaignId, officeId, regionId, query, supplyId, hasDeviation, page, size);
    }

    @GetMapping("/supply")
    public PartnerOfficeSupplyPageResponse supply(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean hasDeviation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.searchSupply(userContext(headers), officeId, regionId, status, hasDeviation, page, size);
    }

    @GetMapping("/supply/{supplyId}")
    public PartnerOfficeSupplyDetailsResponse supplyDetails(@RequestHeader HttpHeaders headers, @PathVariable String supplyId) {
        return service.getSupply(userContext(headers), supplyId);
    }

    @PostMapping("/supply/{supplyId}/transition")
    public PartnerOfficeActionResponse transitionSupply(
            @RequestHeader HttpHeaders headers,
            @PathVariable String supplyId,
            @RequestBody PartnerOfficeSupplyTransitionRequest request
    ) {
        return service.transitionSupply(userContext(headers), supplyId, request, idempotencyKey(headers));
    }

    @GetMapping("/supply/orders/{orderNumber}")
    public PartnerOfficeSupplyOrderDetailsResponse supplyOrder(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber) {
        return service.getSupplyOrder(userContext(headers), orderNumber);
    }

    @PostMapping("/supply/orders/{orderNumber}/deviations")
    public PartnerOfficeActionResponse recordDeviation(
            @RequestHeader HttpHeaders headers,
            @PathVariable String orderNumber,
            @RequestBody PartnerOfficeDeviationCreateRequest request
    ) {
        return service.recordDeviation(userContext(headers), orderNumber, request, idempotencyKey(headers));
    }

    @GetMapping("/report")
    public PartnerOfficeReportResponse report(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String officeId,
            @RequestParam(required = false) String regionId
    ) {
        return service.report(userContext(headers), officeId, regionId);
    }

    @ExceptionHandler(PartnerOfficeAccessDeniedException.class)
    public ResponseEntity<PartnerOfficeErrorResponse> handleForbidden(PartnerOfficeAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(PartnerOfficeNotFoundException.class)
    public ResponseEntity<PartnerOfficeErrorResponse> handleNotFound(PartnerOfficeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
    }

    @ExceptionHandler(PartnerOfficeValidationException.class)
    public ResponseEntity<PartnerOfficeErrorResponse> handleValidation(PartnerOfficeValidationException ex) {
        HttpStatus status = ex.statusCode() == 409 ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(ex.getMessage()));
    }

    private static PartnerOfficeErrorResponse error(String code) {
        return new PartnerOfficeErrorResponse(code, List.of(new PartnerOfficeValidationReasonResponse(code, "BLOCKING", "partner-office")), Map.of());
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
