package com.bestorigin.monolith.partnerbenefits.impl.controller;

import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitApplyPreviewRequest;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitApplyPreviewResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.PartnerBenefitsErrorResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.PartnerBenefitsSummaryResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.PartnerBenefitsValidationReasonResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralEventPageResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralLinkResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardPageResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardRedemptionRequest;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardRedemptionResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.SupportTimelineResponse;
import com.bestorigin.monolith.partnerbenefits.impl.service.PartnerBenefitsAccessDeniedException;
import com.bestorigin.monolith.partnerbenefits.impl.service.PartnerBenefitsNotFoundException;
import com.bestorigin.monolith.partnerbenefits.impl.service.PartnerBenefitsService;
import com.bestorigin.monolith.partnerbenefits.impl.service.PartnerBenefitsValidationException;
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
@RequestMapping("/api/partner-benefits")
public class PartnerBenefitsController {
    private final PartnerBenefitsService service;

    public PartnerBenefitsController(PartnerBenefitsService service) {
        this.service = service;
    }

    @GetMapping("/me/summary")
    public PartnerBenefitsSummaryResponse summary(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String catalogId) {
        return service.summary(userContext(headers), catalogId);
    }

    @PostMapping("/me/benefits/{benefitId}/apply-preview")
    public BenefitApplyPreviewResponse applyPreview(@RequestHeader HttpHeaders headers, @PathVariable UUID benefitId, @RequestBody BenefitApplyPreviewRequest request) {
        return service.applyPreview(userContext(headers), benefitId, request);
    }

    @GetMapping("/me/referral-link")
    public ReferralLinkResponse referralLink(@RequestHeader HttpHeaders headers) {
        return service.referralLink(userContext(headers));
    }

    @GetMapping("/me/referral-events")
    public ReferralEventPageResponse referralEvents(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.referralEvents(userContext(headers), status, page, size);
    }

    @GetMapping("/me/rewards")
    public RewardPageResponse rewards(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String catalogId,
            @RequestParam(defaultValue = "true") boolean onlyAvailable
    ) {
        return service.rewards(userContext(headers), catalogId, onlyAvailable);
    }

    @PostMapping("/me/rewards/{rewardId}/redemptions")
    public ResponseEntity<RewardRedemptionResponse> redeemReward(
            @RequestHeader HttpHeaders headers,
            @PathVariable UUID rewardId,
            @RequestBody RewardRedemptionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.redeemReward(userContext(headers), rewardId, request, idempotencyKey(headers)));
    }

    @GetMapping("/support/accounts/{partnerNumber}/timeline")
    public SupportTimelineResponse supportTimeline(
            @RequestHeader HttpHeaders headers,
            @PathVariable String partnerNumber,
            @RequestParam(required = false) String eventType
    ) {
        return service.supportTimeline(userContext(headers), partnerNumber, eventType);
    }

    @ExceptionHandler(PartnerBenefitsAccessDeniedException.class)
    public ResponseEntity<PartnerBenefitsErrorResponse> handleForbidden(PartnerBenefitsAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), "authorization"));
    }

    @ExceptionHandler(PartnerBenefitsNotFoundException.class)
    public ResponseEntity<PartnerBenefitsErrorResponse> handleNotFound(PartnerBenefitsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage(), "partnerBenefits"));
    }

    @ExceptionHandler(PartnerBenefitsValidationException.class)
    public ResponseEntity<PartnerBenefitsErrorResponse> handleValidation(PartnerBenefitsValidationException ex) {
        HttpStatus status = ex.statusCode() == 409 ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(ex.getMessage(), "partnerBenefits"));
    }

    private static PartnerBenefitsErrorResponse error(String code, String target) {
        return new PartnerBenefitsErrorResponse(code, "CORR-040-ERROR", List.of(new PartnerBenefitsValidationReasonResponse(target, code)), Map.of());
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
        return value == null || value.isBlank() ? "implicit-040" : value;
    }
}
