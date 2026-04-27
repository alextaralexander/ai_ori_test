package com.bestorigin.monolith.adminreferral.impl.controller;

import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AdminReferralErrorResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionEventResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionOverrideRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionPolicyResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionPolicyUpdateRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AuditResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ConversionReportResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelDetailResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelUpsertRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingDetailResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingUpsertRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.PreviewResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeGenerateRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeResponse;
import com.bestorigin.monolith.adminreferral.impl.exception.AdminReferralAccessDeniedException;
import com.bestorigin.monolith.adminreferral.impl.exception.AdminReferralConflictException;
import com.bestorigin.monolith.adminreferral.impl.exception.AdminReferralValidationException;
import com.bestorigin.monolith.adminreferral.impl.service.AdminReferralService;
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
@RequestMapping("/api/admin-referral")
public class AdminReferralController {

    private final AdminReferralService service;

    public AdminReferralController(AdminReferralService service) {
        this.service = service;
    }

    @GetMapping("/landing-variants")
    public LandingListResponse landingVariants(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String landingType, @RequestParam(required = false) String status, @RequestParam(required = false) String campaignCode, @RequestParam(required = false) String locale, @RequestParam(required = false) String search) {
        return service.searchLandings(token(headers), landingType, status, campaignCode, locale, search);
    }

    @PostMapping("/landing-variants")
    public ResponseEntity<LandingDetailResponse> createLanding(@RequestHeader HttpHeaders headers, @RequestHeader(value = "X-Elevated-Session-Id", required = false) String elevatedSessionId, @RequestBody LandingUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLanding(token(headers), elevatedSessionId, request));
    }

    @GetMapping("/landing-variants/{landingId}")
    public LandingDetailResponse landing(@RequestHeader HttpHeaders headers, @PathVariable UUID landingId) {
        return service.getLanding(token(headers), landingId);
    }

    @PutMapping("/landing-variants/{landingId}")
    public LandingDetailResponse updateLanding(@RequestHeader HttpHeaders headers, @PathVariable UUID landingId, @RequestBody LandingUpsertRequest request) {
        return service.updateLanding(token(headers), landingId, request);
    }

    @PostMapping("/landing-variants/{landingId}/activate")
    public LandingDetailResponse activateLanding(@RequestHeader HttpHeaders headers, @PathVariable UUID landingId) {
        return service.activateLanding(token(headers), landingId);
    }

    @PostMapping("/landing-variants/{landingId}/preview")
    public PreviewResponse preview(@RequestHeader HttpHeaders headers, @PathVariable UUID landingId) {
        return service.previewLanding(token(headers), landingId);
    }

    @GetMapping("/funnels")
    public FunnelListResponse funnels(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String scenario, @RequestParam(required = false) String status) {
        return service.searchFunnels(token(headers), scenario, status);
    }

    @PostMapping("/funnels")
    public ResponseEntity<FunnelDetailResponse> createFunnel(@RequestHeader HttpHeaders headers, @RequestBody FunnelUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFunnel(token(headers), request));
    }

    @PostMapping("/funnels/{funnelId}/activate")
    public FunnelDetailResponse activateFunnel(@RequestHeader HttpHeaders headers, @PathVariable UUID funnelId) {
        return service.activateFunnel(token(headers), funnelId);
    }

    @GetMapping("/referral-codes")
    public ReferralCodeListResponse referralCodes(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignCode, @RequestParam(required = false) UUID ownerPartnerId, @RequestParam(required = false) String codeType, @RequestParam(required = false) String status) {
        return service.searchReferralCodes(token(headers), campaignCode, ownerPartnerId, codeType, status);
    }

    @PostMapping("/referral-codes")
    public ResponseEntity<ReferralCodeResponse> generateReferralCode(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ReferralCodeGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generateReferralCode(token(headers), idempotencyKey, request));
    }

    @PostMapping("/referral-codes/{referralCodeId}/revoke")
    public ReferralCodeResponse revokeReferralCode(@RequestHeader HttpHeaders headers, @PathVariable UUID referralCodeId) {
        return service.revokeReferralCode(token(headers), referralCodeId);
    }

    @GetMapping("/attribution-policy")
    public AttributionPolicyResponse attributionPolicy(@RequestHeader HttpHeaders headers) {
        return service.getAttributionPolicy(token(headers));
    }

    @PutMapping("/attribution-policy")
    public AttributionPolicyResponse updateAttributionPolicy(@RequestHeader HttpHeaders headers, @RequestBody AttributionPolicyUpdateRequest request) {
        return service.updateAttributionPolicy(token(headers), request);
    }

    @PostMapping("/attribution/override")
    public AttributionEventResponse overrideAttribution(@RequestHeader HttpHeaders headers, @RequestBody AttributionOverrideRequest request) {
        return service.overrideAttribution(token(headers), request);
    }

    @GetMapping("/analytics/conversions")
    public ConversionReportResponse conversionReport(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignCode, @RequestParam(required = false) UUID landingId, @RequestParam(required = false) String sourceChannel, @RequestParam(required = false) String dateFrom, @RequestParam(required = false) String dateTo) {
        return service.conversionReport(token(headers), campaignCode, landingId, sourceChannel, dateFrom, dateTo);
    }

    @GetMapping("/audit")
    public AuditResponse audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String entityType, @RequestParam(required = false) UUID entityId, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String correlationId) {
        return service.audit(token(headers), entityType, entityId, actionCode, correlationId);
    }

    @ExceptionHandler(AdminReferralAccessDeniedException.class)
    public ResponseEntity<AdminReferralErrorResponse> handleForbidden(AdminReferralAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AdminReferralConflictException.class)
    public ResponseEntity<AdminReferralErrorResponse> handleConflict(AdminReferralConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AdminReferralValidationException.class)
    public ResponseEntity<AdminReferralErrorResponse> handleValidation(AdminReferralValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
    }

    private static AdminReferralErrorResponse error(String messageCode) {
        return new AdminReferralErrorResponse(messageCode, "CORR-028-ERROR");
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
