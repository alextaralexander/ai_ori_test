package com.bestorigin.monolith.partnerbenefits.domain;

import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitGrantResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralEventResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralLinkResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RetentionOfferResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardItemResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.SupportTimelineEventResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PartnerBenefitsSnapshot(
        UUID accountId,
        UUID partnerId,
        String partnerNumber,
        String accountStatus,
        String catalogId,
        String currentTier,
        BigDecimal rewardBalance,
        BigDecimal cashbackPending,
        BigDecimal cashbackConfirmed,
        List<BenefitGrantResponse> benefits,
        ReferralLinkResponse referralLink,
        List<ReferralEventResponse> referralEvents,
        List<RewardItemResponse> rewards,
        List<RetentionOfferResponse> retentionOffers,
        List<SupportTimelineEventResponse> timeline
) {
}
