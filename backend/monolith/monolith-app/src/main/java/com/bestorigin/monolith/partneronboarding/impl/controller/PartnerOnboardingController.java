package com.bestorigin.monolith.partneronboarding.impl.controller;

import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationCompleteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationCompleteResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationStateResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ContactConfirmationRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.CreateInviteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ErrorResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteValidationResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.OnboardingType;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.RegistrationApplicationRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.RegistrationApplicationResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ResendInviteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.SponsorInviteListResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.SponsorInviteResponse;
import com.bestorigin.monolith.partneronboarding.impl.service.PartnerOnboardingNotFoundException;
import com.bestorigin.monolith.partneronboarding.impl.service.PartnerOnboardingService;
import java.util.Map;
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
@RequestMapping("/api/partner-onboarding")
public class PartnerOnboardingController {

    private final PartnerOnboardingService service;

    public PartnerOnboardingController(PartnerOnboardingService service) {
        this.service = service;
    }

    @GetMapping("/invites/validate")
    public InviteValidationResponse validateInvite(
            @RequestParam String code,
            @RequestParam OnboardingType onboardingType,
            @RequestParam(required = false) String campaignId
    ) {
        return service.validateInvite(code, onboardingType, campaignId);
    }

    @PostMapping("/registrations")
    public ResponseEntity<RegistrationApplicationResponse> createRegistration(
            @RequestBody RegistrationApplicationRequest request,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRegistration(request, idempotencyKey));
    }

    @GetMapping("/activations/{token}")
    public ActivationStateResponse getActivation(@PathVariable String token) {
        return service.getActivation(token);
    }

    @PostMapping("/activations/{token}/confirm-contact")
    public ActivationStateResponse confirmContact(
            @PathVariable String token,
            @RequestBody ContactConfirmationRequest request
    ) {
        return service.confirmContact(token, request);
    }

    @PostMapping("/activations/{token}/complete")
    public ActivationCompleteResponse completeActivation(
            @PathVariable String token,
            @RequestBody ActivationCompleteRequest request,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey
    ) {
        return service.completeActivation(token, request, idempotencyKey);
    }

    @GetMapping("/sponsor-cabinet/invites")
    public SponsorInviteListResponse getSponsorInvites(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String onboardingType
    ) {
        return service.getSponsorInvites(sponsorContext(authorization));
    }

    @PostMapping("/sponsor-cabinet/invites")
    public ResponseEntity<SponsorInviteResponse> createSponsorInvite(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @RequestBody CreateInviteRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSponsorInvite(request, sponsorContext(authorization), idempotencyKey));
    }

    @PostMapping("/sponsor-cabinet/invites/{inviteId}/resend")
    public SponsorInviteResponse resendInvite(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable String inviteId,
            @RequestBody(required = false) ResendInviteRequest request
    ) {
        return service.resendInvite(inviteId, request, sponsorContext(authorization));
    }

    @ExceptionHandler(PartnerOnboardingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PartnerOnboardingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("PARTNER_ONBOARDING_NOT_FOUND", ex.getMessage(), Map.of()));
    }

    private static String sponsorContext(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return "partner-maria";
        }
        return authorization.replace("Bearer ", "");
    }
}
