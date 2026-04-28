package com.bestorigin.monolith.partnerbenefits.impl.service;

import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitApplyPreviewRequest;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitApplyPreviewResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.MoneyAmount;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.PartnerBenefitsSummaryResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralEventPageResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralLinkResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardPageResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardRedemptionRequest;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardRedemptionResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.SupportTimelineResponse;
import com.bestorigin.monolith.partnerbenefits.domain.PartnerBenefitsRepository;
import com.bestorigin.monolith.partnerbenefits.domain.PartnerBenefitsSnapshot;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultPartnerBenefitsService implements PartnerBenefitsService {
    private static final String ACCESS_DENIED = "STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED";

    private final PartnerBenefitsRepository repository;

    public DefaultPartnerBenefitsService(PartnerBenefitsRepository repository) {
        this.repository = repository;
    }

    @Override
    public PartnerBenefitsSummaryResponse summary(String userContext, String catalogId) {
        requirePartner(userContext);
        PartnerBenefitsSnapshot snapshot = repository.defaultSnapshot();
        return new PartnerBenefitsSummaryResponse(
                snapshot.accountId(),
                snapshot.partnerId(),
                snapshot.partnerNumber(),
                snapshot.accountStatus(),
                catalogId == null || catalogId.isBlank() ? snapshot.catalogId() : catalogId,
                snapshot.currentTier(),
                MoneyAmount.rub(snapshot.rewardBalance()),
                MoneyAmount.rub(snapshot.cashbackPending()),
                MoneyAmount.rub(snapshot.cashbackConfirmed()),
                snapshot.benefits(),
                snapshot.referralLink(),
                snapshot.retentionOffers()
        );
    }

    @Override
    public BenefitApplyPreviewResponse applyPreview(String userContext, UUID benefitId, BenefitApplyPreviewRequest request) {
        requirePartner(userContext);
        if (request == null || request.target() == null || request.target().isBlank()) {
            throw new PartnerBenefitsValidationException("STR_MNEMO_PARTNER_BENEFITS_VALIDATION_FAILED", 400);
        }
        boolean exists = repository.defaultSnapshot().benefits().stream().anyMatch(benefit -> benefit.benefitId().equals(benefitId));
        if (!exists) {
            throw new PartnerBenefitsNotFoundException("STR_MNEMO_PARTNER_BENEFITS_NOT_FOUND");
        }
        return new BenefitApplyPreviewResponse(true, benefitId, request.target(), "STR_MNEMO_PARTNER_BENEFITS_APPLICABLE", MoneyAmount.rub(new BigDecimal("199.00")));
    }

    @Override
    public ReferralLinkResponse referralLink(String userContext) {
        requirePartner(userContext);
        return repository.defaultSnapshot().referralLink();
    }

    @Override
    public ReferralEventPageResponse referralEvents(String userContext, String status, int page, int size) {
        requirePartner(userContext);
        var items = repository.defaultSnapshot().referralEvents().stream()
                .filter(event -> status == null || status.isBlank() || status.equals(event.eventStatus()))
                .toList();
        return new ReferralEventPageResponse(items, page, size, items.size());
    }

    @Override
    public RewardPageResponse rewards(String userContext, String catalogId, boolean onlyAvailable) {
        requirePartner(userContext);
        return new RewardPageResponse(repository.defaultSnapshot().rewards());
    }

    @Override
    public RewardRedemptionResponse redeemReward(String userContext, UUID rewardId, RewardRedemptionRequest request, String idempotencyKey) {
        requirePartner(userContext);
        var reward = repository.defaultSnapshot().rewards().stream()
                .filter(item -> item.rewardId().equals(rewardId))
                .findFirst()
                .orElseThrow(() -> new PartnerBenefitsNotFoundException("STR_MNEMO_PARTNER_BENEFITS_NOT_FOUND"));
        BigDecimal expected = request == null ? null : request.expectedCostPoints();
        if (expected != null && expected.compareTo(reward.costPoints()) != 0) {
            throw new PartnerBenefitsValidationException("STR_MNEMO_PARTNER_BENEFITS_VERSION_CONFLICT", 409);
        }
        String correlationId = "CORR-040-REWARD";
        repository.saveRedemption(rewardId, idempotencyKey == null || idempotencyKey.isBlank() ? "implicit-040" : idempotencyKey, correlationId);
        return new RewardRedemptionResponse(UUID.fromString("00000000-0040-0000-0000-000000000201"), rewardId, "RESERVED", reward.costPoints(), correlationId);
    }

    @Override
    public SupportTimelineResponse supportTimeline(String userContext, String partnerNumber, String eventType) {
        requireSupport(userContext);
        PartnerBenefitsSnapshot snapshot = repository.findByPartnerNumber(partnerNumber)
                .orElseThrow(() -> new PartnerBenefitsNotFoundException("STR_MNEMO_PARTNER_BENEFITS_NOT_FOUND"));
        return new SupportTimelineResponse(snapshot.partnerNumber(), snapshot.timeline());
    }

    private static void requirePartner(String userContext) {
        if (userContext == null || !(userContext.contains("partner") || userContext.contains("business"))) {
            throw new PartnerBenefitsAccessDeniedException(ACCESS_DENIED);
        }
    }

    private static void requireSupport(String userContext) {
        if (userContext == null || !(userContext.contains("partner-support") || userContext.contains("employee") || userContext.contains("supervisor") || userContext.contains("admin"))) {
            throw new PartnerBenefitsAccessDeniedException(ACCESS_DENIED);
        }
    }
}
