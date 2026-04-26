package com.bestorigin.monolith.partneronboarding.impl.service;

import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationCompleteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationCompleteResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationStateResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ApplicationStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ContactConfirmationRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.CreateInviteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteValidationResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteValidationStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.OnboardingType;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.PublicSponsorContext;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ReferralLinkResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.RegistrationApplicationRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.RegistrationApplicationResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ResendInviteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.SponsorInviteListResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.SponsorInviteResponse;
import com.bestorigin.monolith.partneronboarding.domain.PartnerOnboardingRepository;
import com.bestorigin.monolith.partneronboarding.domain.PartnerOnboardingState.Application;
import com.bestorigin.monolith.partneronboarding.domain.PartnerOnboardingState.Invite;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultPartnerOnboardingService implements PartnerOnboardingService {

    private final PartnerOnboardingRepository repository;

    public DefaultPartnerOnboardingService(PartnerOnboardingRepository repository) {
        this.repository = repository;
    }

    @Override
    public InviteValidationResponse validateInvite(String code, OnboardingType onboardingType, String campaignId) {
        return repository.findInviteByCode(code)
                .filter(invite -> invite.onboardingType() == onboardingType)
                .filter(invite -> invite.expiresAt().isAfter(Instant.now()))
                .map(invite -> new InviteValidationResponse(
                        InviteValidationStatus.ACTIVE,
                        onboardingType,
                        campaignId == null || campaignId.isBlank() ? invite.campaignId() : campaignId,
                        sponsor(),
                        "STR_MNEMO_INVITE_CODE_ACTIVE"
                ))
                .orElseGet(() -> new InviteValidationResponse(
                        InviteValidationStatus.NOT_FOUND,
                        onboardingType,
                        campaignId,
                        null,
                        "STR_MNEMO_INVITE_CODE_INVALID"
                ));
    }

    @Override
    public RegistrationApplicationResponse createRegistration(RegistrationApplicationRequest request, String idempotencyKey) {
        Application application = new Application(
                UUID.fromString("20000000-0000-0000-0000-000000000008"),
                "APP-008-001",
                normalize(request.inviteCode()),
                "partner-maria",
                request.onboardingType(),
                ApplicationStatus.PENDING_CONTACT_CONFIRMATION,
                request.candidateName(),
                request.campaignId() == null || request.campaignId().isBlank() ? "CMP-2026-05" : request.campaignId(),
                false,
                false
        );
        repository.saveApplication(application);
        return new RegistrationApplicationResponse(
                application.id(),
                application.applicationNumber(),
                application.status(),
                "CONFIRM_CONTACT",
                "/invite/partners-activation?token=ACT-008-001",
                "STR_MNEMO_REGISTRATION_APPLICATION_CREATED"
        );
    }

    @Override
    public ActivationStateResponse getActivation(String token) {
        Application application = applicationFor(token);
        return new ActivationStateResponse(
                application.id(),
                application.status(),
                application.contactConfirmed(),
                application.termsAccepted(),
                sponsor(),
                "STR_MNEMO_ACTIVATION_READY"
        );
    }

    @Override
    public ActivationStateResponse confirmContact(String token, ContactConfirmationRequest request) {
        Application existing = applicationFor(token);
        Application updated = new Application(
                existing.id(),
                existing.applicationNumber(),
                existing.inviteCode(),
                existing.sponsorPartnerId(),
                existing.onboardingType(),
                ApplicationStatus.READY_FOR_ACTIVATION,
                existing.candidateName(),
                existing.campaignId(),
                true,
                existing.termsAccepted()
        );
        repository.saveApplicationForToken(token, updated);
        return new ActivationStateResponse(updated.id(), updated.status(), true, updated.termsAccepted(), sponsor(), "STR_MNEMO_ACTIVATION_READY");
    }

    @Override
    public ActivationCompleteResponse completeActivation(String token, ActivationCompleteRequest request, String idempotencyKey) {
        Application existing = applicationFor(token);
        Application updated = new Application(
                existing.id(),
                existing.applicationNumber(),
                existing.inviteCode(),
                existing.sponsorPartnerId(),
                existing.onboardingType(),
                ApplicationStatus.ACTIVE,
                existing.candidateName(),
                existing.campaignId(),
                true,
                true
        );
        repository.saveApplicationForToken(token, updated);
        return new ActivationCompleteResponse(
                UUID.fromString("30000000-0000-0000-0000-000000000008"),
                "BOG-P-0008",
                "ACTIVE",
                new ReferralLinkResponse("BOG778", "/business-benefits/BOG778"),
                "STR_MNEMO_PARTNER_ACTIVATED"
        );
    }

    @Override
    public SponsorInviteListResponse getSponsorInvites(String sponsorContext) {
        List<SponsorInviteResponse> items = repository.findInvitesBySponsor(sponsorContext).stream()
                .map(this::toSponsorInviteResponse)
                .toList();
        return new SponsorInviteListResponse(items);
    }

    @Override
    public SponsorInviteResponse createSponsorInvite(CreateInviteRequest request, String sponsorContext, String idempotencyKey) {
        String code = "BOG" + Math.abs(idempotencyKey.hashCode() % 900 + 100);
        Invite invite = new Invite(
                UUID.nameUUIDFromBytes(idempotencyKey.getBytes()),
                code,
                sponsorContext,
                request.onboardingType(),
                request.campaignId() == null || request.campaignId().isBlank() ? "CMP-2026-05" : request.campaignId(),
                InviteStatus.CREATED,
                Instant.now().plus(21, ChronoUnit.DAYS),
                request.candidatePublicName(),
                null
        );
        return toSponsorInviteResponse(repository.saveInvite(invite));
    }

    @Override
    public SponsorInviteResponse resendInvite(String inviteId, ResendInviteRequest request, String sponsorContext) {
        return repository.findInvitesBySponsor(sponsorContext).stream()
                .filter(invite -> invite.id().toString().equals(inviteId))
                .findFirst()
                .map(this::toSponsorInviteResponse)
                .orElseThrow(() -> new PartnerOnboardingNotFoundException("STR_MNEMO_INVITE_CODE_INVALID"));
    }

    private SponsorInviteResponse toSponsorInviteResponse(Invite invite) {
        return new SponsorInviteResponse(
                invite.id(),
                invite.code(),
                invite.onboardingType(),
                invite.status(),
                "/invite/" + invite.onboardingType().name().toLowerCase(Locale.ROOT).replace('_', '-') + "?code=" + invite.code(),
                invite.candidatePublicName(),
                invite.expiresAt(),
                invite.lastOpenedAt(),
                "STR_MNEMO_INVITE_CREATED"
        );
    }

    private Application applicationFor(String token) {
        return repository.findApplicationByToken(token)
                .orElseThrow(() -> new PartnerOnboardingNotFoundException("STR_MNEMO_ACTIVATION_TOKEN_EXPIRED"));
    }

    private static PublicSponsorContext sponsor() {
        return new PublicSponsorContext("public.referral.sponsor.maria", "BOG777", null);
    }

    private static String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }
}
