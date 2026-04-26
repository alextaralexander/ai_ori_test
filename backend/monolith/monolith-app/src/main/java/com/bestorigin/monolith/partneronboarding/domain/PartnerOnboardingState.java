package com.bestorigin.monolith.partneronboarding.domain;

import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ApplicationStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.OnboardingType;
import java.time.Instant;
import java.util.UUID;

public final class PartnerOnboardingState {

    private PartnerOnboardingState() {
    }

    public record Invite(
            UUID id,
            String code,
            String sponsorPartnerId,
            OnboardingType onboardingType,
            String campaignId,
            InviteStatus status,
            Instant expiresAt,
            String candidatePublicName,
            Instant lastOpenedAt
    ) {
    }

    public record Application(
            UUID id,
            String applicationNumber,
            String inviteCode,
            String sponsorPartnerId,
            OnboardingType onboardingType,
            ApplicationStatus status,
            String candidateName,
            String campaignId,
            boolean contactConfirmed,
            boolean termsAccepted
    ) {
    }
}
