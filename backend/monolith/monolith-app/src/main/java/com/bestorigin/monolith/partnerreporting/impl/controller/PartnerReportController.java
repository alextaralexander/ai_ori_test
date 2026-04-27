package com.bestorigin.monolith.partnerreporting.impl.controller;

import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerCommissionDetailResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentDownloadResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentLifecycleRequest;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentPageResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportErrorResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportRequest;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportFinanceReconciliationResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportOrderPageResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportPrintViewResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportSummaryResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportValidationReasonResponse;
import com.bestorigin.monolith.partnerreporting.impl.service.PartnerReportAccessDeniedException;
import com.bestorigin.monolith.partnerreporting.impl.service.PartnerReportNotFoundException;
import com.bestorigin.monolith.partnerreporting.impl.service.PartnerReportService;
import com.bestorigin.monolith.partnerreporting.impl.service.PartnerReportValidationException;
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
@RequestMapping("/api/partner-reporting")
public class PartnerReportController {

    private final PartnerReportService service;

    public PartnerReportController(PartnerReportService service) {
        this.service = service;
    }

    @GetMapping("/reports/summary")
    public PartnerReportSummaryResponse summary(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String catalogId,
            @RequestParam(required = false) String bonusProgramId
    ) {
        return service.summary(userContext(headers), dateFrom, dateTo, catalogId, bonusProgramId);
    }

    @GetMapping("/reports/orders")
    public PartnerReportOrderPageResponse orderLines(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String catalogId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String payoutStatus,
            @RequestParam(required = false) String bonusProgramId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.orderLines(userContext(headers), dateFrom, dateTo, catalogId, orderNumber, payoutStatus, bonusProgramId, page, size);
    }

    @GetMapping("/reports/orders/{orderNumber}/commission")
    public PartnerCommissionDetailResponse commissionDetails(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber) {
        return service.commissionDetails(userContext(headers), orderNumber);
    }

    @GetMapping("/documents")
    public PartnerReportDocumentPageResponse documents(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.documents(userContext(headers), dateFrom, dateTo, documentType, documentStatus, page, size);
    }

    @PostMapping("/documents/{documentId}/download")
    public PartnerReportDocumentDownloadResponse download(@RequestHeader HttpHeaders headers, @PathVariable UUID documentId) {
        return service.download(userContext(headers), documentId);
    }

    @GetMapping("/documents/{documentId}/print-view")
    public PartnerReportPrintViewResponse printView(@RequestHeader HttpHeaders headers, @PathVariable UUID documentId) {
        return service.printView(userContext(headers), documentId);
    }

    @PostMapping("/exports")
    public PartnerReportExportResponse exportReport(@RequestHeader HttpHeaders headers, @RequestBody PartnerReportExportRequest request) {
        return service.exportReport(userContext(headers), request);
    }

    @GetMapping("/finance/reconciliations")
    public PartnerReportFinanceReconciliationResponse financeReconciliation(
            @RequestHeader HttpHeaders headers,
            @RequestParam String partnerId,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String reason
    ) {
        return service.financeReconciliation(userContext(headers), partnerId, dateFrom, dateTo, reason);
    }

    @PostMapping("/finance/documents/{documentId}/publish")
    public PartnerReportDocumentResponse publishDocument(
            @RequestHeader HttpHeaders headers,
            @PathVariable UUID documentId,
            @RequestBody PartnerReportDocumentLifecycleRequest request
    ) {
        return service.publishDocument(userContext(headers), documentId, request);
    }

    @PostMapping("/finance/documents/{documentId}/revoke")
    public PartnerReportDocumentResponse revokeDocument(
            @RequestHeader HttpHeaders headers,
            @PathVariable UUID documentId,
            @RequestBody PartnerReportDocumentLifecycleRequest request
    ) {
        return service.revokeDocument(userContext(headers), documentId, request);
    }

    @ExceptionHandler(PartnerReportAccessDeniedException.class)
    public ResponseEntity<PartnerReportErrorResponse> handleForbidden(PartnerReportAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), "partner-reporting"));
    }

    @ExceptionHandler(PartnerReportNotFoundException.class)
    public ResponseEntity<PartnerReportErrorResponse> handleNotFound(PartnerReportNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage(), "partner-reporting"));
    }

    @ExceptionHandler(PartnerReportValidationException.class)
    public ResponseEntity<PartnerReportErrorResponse> handleValidation(PartnerReportValidationException ex) {
        HttpStatus status = ex.statusCode() == 409 ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(ex.getMessage(), "partner-reporting"));
    }

    private static PartnerReportErrorResponse error(String code, String target) {
        return new PartnerReportErrorResponse(code, List.of(new PartnerReportValidationReasonResponse(code, "BLOCKING", target)), Map.of());
    }

    private static String userContext(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        return value.replace("Bearer ", "").trim();
    }
}
