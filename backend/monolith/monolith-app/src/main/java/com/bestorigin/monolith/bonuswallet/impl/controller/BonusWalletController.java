package com.bestorigin.monolith.bonuswallet.impl.controller;

import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletApplyLimitResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletErrorResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletExportRequest;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletExportResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletManualAdjustmentRequest;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletSummaryResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionDetailsResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletTransactionPageResponse;
import com.bestorigin.monolith.bonuswallet.api.BonusWalletDtos.BonusWalletValidationReasonResponse;
import com.bestorigin.monolith.bonuswallet.impl.service.BonusWalletAccessDeniedException;
import com.bestorigin.monolith.bonuswallet.impl.service.BonusWalletNotFoundException;
import com.bestorigin.monolith.bonuswallet.impl.service.BonusWalletService;
import com.bestorigin.monolith.bonuswallet.impl.service.BonusWalletValidationException;
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
@RequestMapping("/api/bonus-wallet")
public class BonusWalletController {

    private final BonusWalletService service;

    public BonusWalletController(BonusWalletService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public BonusWalletSummaryResponse summary(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String type) {
        return service.summary(userContext(headers), type);
    }

    @GetMapping("/transactions")
    public BonusWalletTransactionPageResponse transactions(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.transactions(userContext(headers), type, status, campaignId, sourceType, orderNumber, page, size);
    }

    @GetMapping("/transactions/{transactionId}")
    public BonusWalletTransactionDetailsResponse details(@RequestHeader HttpHeaders headers, @PathVariable String transactionId) {
        return service.details(userContext(headers), transactionId);
    }

    @GetMapping("/limits/order/{orderNumber}")
    public BonusWalletApplyLimitResponse orderLimit(@RequestHeader HttpHeaders headers, @PathVariable String orderNumber) {
        return service.orderLimit(userContext(headers), orderNumber);
    }

    @PostMapping("/exports")
    public BonusWalletExportResponse exportHistory(@RequestHeader HttpHeaders headers, @RequestBody BonusWalletExportRequest request) {
        return service.exportHistory(userContext(headers), request);
    }

    @GetMapping("/finance/{targetUserId}")
    public BonusWalletSummaryResponse financeSummary(
            @RequestHeader HttpHeaders headers,
            @PathVariable String targetUserId,
            @RequestParam(required = false) String reason
    ) {
        return service.financeSummary(userContext(headers), targetUserId, reason);
    }

    @PostMapping("/finance/adjustments")
    public BonusWalletTransactionDetailsResponse manualAdjustment(
            @RequestHeader HttpHeaders headers,
            @RequestBody BonusWalletManualAdjustmentRequest request
    ) {
        return service.manualAdjustment(userContext(headers), request, idempotencyKey(headers));
    }

    @ExceptionHandler(BonusWalletAccessDeniedException.class)
    public ResponseEntity<BonusWalletErrorResponse> handleForbidden(BonusWalletAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), "wallet"));
    }

    @ExceptionHandler(BonusWalletNotFoundException.class)
    public ResponseEntity<BonusWalletErrorResponse> handleNotFound(BonusWalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage(), "transaction"));
    }

    @ExceptionHandler(BonusWalletValidationException.class)
    public ResponseEntity<BonusWalletErrorResponse> handleValidation(BonusWalletValidationException ex) {
        HttpStatus status = ex.statusCode() == 409 ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(ex.getMessage(), "wallet"));
    }

    private static BonusWalletErrorResponse error(String code, String target) {
        return new BonusWalletErrorResponse(code, List.of(new BonusWalletValidationReasonResponse(code, "BLOCKING", target)), Map.of());
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
