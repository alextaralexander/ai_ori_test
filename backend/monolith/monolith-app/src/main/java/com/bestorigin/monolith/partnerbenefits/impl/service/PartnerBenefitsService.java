package com.bestorigin.monolith.partnerbenefits.impl.service;

import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitApplyPreviewRequest;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitApplyPreviewResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.PartnerBenefitsSummaryResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralEventPageResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralLinkResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardPageResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardRedemptionRequest;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardRedemptionResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.SupportTimelineResponse;
import java.util.UUID;

public interface PartnerBenefitsService {
    PartnerBenefitsSummaryResponse summary(String userContext, String catalogId);

    BenefitApplyPreviewResponse applyPreview(String userContext, UUID benefitId, BenefitApplyPreviewRequest request);

    ReferralLinkResponse referralLink(String userContext);

    ReferralEventPageResponse referralEvents(String userContext, String status, int page, int size);

    RewardPageResponse rewards(String userContext, String catalogId, boolean onlyAvailable);

    RewardRedemptionResponse redeemReward(String userContext, UUID rewardId, RewardRedemptionRequest request, String idempotencyKey);

    SupportTimelineResponse supportTimeline(String userContext, String partnerNumber, String eventType);
}
