package com.bestorigin.monolith.partneronboarding.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PartnerOnboardingDtos {

    private PartnerOnboardingDtos() {
    }

    public enum OnboardingType {
        BEAUTY_PARTNER,
        BUSINESS_PARTNER
    }

    public enum InviteValidationStatus {
        ACTIVE,
        NOT_FOUND,
        EXPIRED,
        DISABLED,
        ALREADY_USED,
        TYPE_MISMATCH
    }

    public enum InviteStatus {
        CREATED,
        OPENED,
        REGISTRATION_STARTED,
        SUBMITTED,
        ACTIVE,
        EXPIRED,
        REJECTED,
        DISABLED
    }

    public enum ApplicationStatus {
        PENDING_CONTACT_CONFIRMATION,
        PENDING_CRM_REVIEW,
        READY_FOR_ACTIVATION,
        ACTIVE,
        REJECTED,
        EXPIRED
    }

    public record PublicSponsorContext(
            String displayNameKey,
            String publicCode,
            String avatarUrl
    ) {
    }

    public record InviteValidationResponse(
            InviteValidationStatus status,
            OnboardingType onboardingType,
            String campaignId,
            PublicSponsorContext sponsor,
            String messageCode
    ) {
    }

    public record ContactRequest(
            String channel,
            String value
    ) {
    }

    public record ConsentAcceptance(
            String code,
            String version,
            boolean accepted
    ) {
    }

    public record RegistrationApplicationRequest(
            OnboardingType onboardingType,
            String inviteCode,
            String candidateName,
            ContactRequest contact,
            String campaignId,
            String landingType,
            String landingVariant,
            String sourceRoute,
            List<ConsentAcceptance> consentVersions
    ) {
    }

    public record RegistrationApplicationResponse(
            UUID applicationId,
            String applicationNumber,
            ApplicationStatus status,
            String nextAction,
            String activationRoute,
            String messageCode
    ) {
    }

    public record ActivationStateResponse(
            UUID applicationId,
            ApplicationStatus status,
            boolean contactConfirmed,
            boolean termsAccepted,
            PublicSponsorContext sponsor,
            String messageCode
    ) {
    }

    public record ContactConfirmationRequest(
            String code
    ) {
    }

    public record ActivationCompleteRequest(
            List<ConsentAcceptance> acceptedTerms
    ) {
    }

    public record ReferralLinkResponse(
            String referralCode,
            String targetRoute
    ) {
    }

    public record ActivationCompleteResponse(
            UUID partnerProfileId,
            String partnerNumber,
            String status,
            ReferralLinkResponse referralLink,
            String messageCode
    ) {
    }

    public record CreateInviteRequest(
            OnboardingType onboardingType,
            String campaignId,
            String candidatePublicName
    ) {
    }

    public record ResendInviteRequest(
            String channel
    ) {
    }

    public record SponsorInviteResponse(
            UUID inviteId,
            String code,
            OnboardingType onboardingType,
            InviteStatus status,
            String targetRoute,
            String candidatePublicName,
            Instant expiresAt,
            Instant lastOpenedAt,
            String messageCode
    ) {
    }

    public record SponsorInviteListResponse(
            List<SponsorInviteResponse> items
    ) {
    }

    public record ErrorResponse(
            String code,
            String messageCode,
            Map<String, String> details
    ) {
    }
}
