package com.bestorigin.monolith.partneronboarding.impl.service;

import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationCompleteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationCompleteResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ActivationStateResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ContactConfirmationRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.CreateInviteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteValidationResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.OnboardingType;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.RegistrationApplicationRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.RegistrationApplicationResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ResendInviteRequest;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.SponsorInviteListResponse;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.SponsorInviteResponse;

public interface PartnerOnboardingService {

    InviteValidationResponse validateInvite(String code, OnboardingType onboardingType, String campaignId);

    RegistrationApplicationResponse createRegistration(RegistrationApplicationRequest request, String idempotencyKey);

    ActivationStateResponse getActivation(String token);

    ActivationStateResponse confirmContact(String token, ContactConfirmationRequest request);

    ActivationCompleteResponse completeActivation(String token, ActivationCompleteRequest request, String idempotencyKey);

    SponsorInviteListResponse getSponsorInvites(String sponsorContext);

    SponsorInviteResponse createSponsorInvite(CreateInviteRequest request, String sponsorContext, String idempotencyKey);

    SponsorInviteResponse resendInvite(String inviteId, ResendInviteRequest request, String sponsorContext);
}
