package com.bestorigin.monolith.partneronboarding.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartnerOnboardingRepository {

    Optional<PartnerOnboardingState.Invite> findInviteByCode(String code);

    List<PartnerOnboardingState.Invite> findInvitesBySponsor(String sponsorPartnerId);

    PartnerOnboardingState.Invite saveInvite(PartnerOnboardingState.Invite invite);

    PartnerOnboardingState.Application saveApplication(PartnerOnboardingState.Application application);

    Optional<PartnerOnboardingState.Application> findApplicationByToken(String token);

    PartnerOnboardingState.Application saveApplicationForToken(String token, PartnerOnboardingState.Application application);
}
