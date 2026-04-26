package com.bestorigin.monolith.partneronboarding.impl.service;

import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.ApplicationStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.InviteStatus;
import com.bestorigin.monolith.partneronboarding.api.PartnerOnboardingDtos.OnboardingType;
import com.bestorigin.monolith.partneronboarding.domain.PartnerOnboardingRepository;
import com.bestorigin.monolith.partneronboarding.domain.PartnerOnboardingState.Application;
import com.bestorigin.monolith.partneronboarding.domain.PartnerOnboardingState.Invite;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPartnerOnboardingRepository implements PartnerOnboardingRepository {

    private final Map<String, Invite> invites = new LinkedHashMap<>();
    private final Map<String, Application> activations = new LinkedHashMap<>();

    public InMemoryPartnerOnboardingRepository() {
        Instant now = Instant.now();
        Invite invite = new Invite(
                UUID.fromString("10000000-0000-0000-0000-000000000008"),
                "BOG777",
                "partner-maria",
                OnboardingType.BUSINESS_PARTNER,
                "CMP-2026-05",
                InviteStatus.CREATED,
                now.plus(21, ChronoUnit.DAYS),
                "Анна",
                now
        );
        invites.put(invite.code(), invite);
        Application application = new Application(
                UUID.fromString("20000000-0000-0000-0000-000000000008"),
                "APP-008-001",
                "BOG777",
                "partner-maria",
                OnboardingType.BUSINESS_PARTNER,
                ApplicationStatus.PENDING_CONTACT_CONFIRMATION,
                "Анна Партнер",
                "CMP-2026-05",
                false,
                false
        );
        activations.put("ACT-008-001", application);
    }

    @Override
    public Optional<Invite> findInviteByCode(String code) {
        return Optional.ofNullable(invites.get(normalize(code)));
    }

    @Override
    public List<Invite> findInvitesBySponsor(String sponsorPartnerId) {
        return new ArrayList<>(invites.values());
    }

    @Override
    public Invite saveInvite(Invite invite) {
        invites.put(invite.code(), invite);
        return invite;
    }

    @Override
    public Application saveApplication(Application application) {
        activations.put("ACT-008-001", application);
        return application;
    }

    @Override
    public Optional<Application> findApplicationByToken(String token) {
        return Optional.ofNullable(activations.get(token));
    }

    @Override
    public Application saveApplicationForToken(String token, Application application) {
        activations.put(token, application);
        return application;
    }

    private static String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }
}
