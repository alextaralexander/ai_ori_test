package com.bestorigin.monolith.partnerbenefits.impl.service;

import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitGrantResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.BenefitProgressResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralEventResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.ReferralLinkResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RetentionOfferResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.RewardItemResponse;
import com.bestorigin.monolith.partnerbenefits.api.PartnerBenefitsDtos.SupportTimelineEventResponse;
import com.bestorigin.monolith.partnerbenefits.domain.PartnerBenefitsRepository;
import com.bestorigin.monolith.partnerbenefits.domain.PartnerBenefitsSnapshot;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPartnerBenefitsRepository implements PartnerBenefitsRepository {
    private final PartnerBenefitsSnapshot defaultSnapshot = seed();
    private final ConcurrentMap<String, String> redemptions = new ConcurrentHashMap<>();

    @Override
    public PartnerBenefitsSnapshot defaultSnapshot() {
        return defaultSnapshot;
    }

    @Override
    public Optional<PartnerBenefitsSnapshot> findByPartnerNumber(String partnerNumber) {
        return "PARTNER-040".equals(partnerNumber) ? Optional.of(defaultSnapshot) : Optional.empty();
    }

    @Override
    public void saveRedemption(UUID rewardId, String idempotencyKey, String correlationId) {
        redemptions.putIfAbsent(idempotencyKey, rewardId + ":" + correlationId);
    }

    private static PartnerBenefitsSnapshot seed() {
        UUID accountId = UUID.fromString("00000000-0040-0000-0000-000000000001");
        UUID partnerId = UUID.fromString("00000000-0040-0000-0000-000000000002");
        UUID welcomeId = UUID.fromString("00000000-0040-0000-0000-000000000011");
        UUID freeDeliveryId = UUID.fromString("00000000-0040-0000-0000-000000000012");
        UUID rewardId = UUID.fromString("00000000-0040-0000-0000-000000000101");
        return new PartnerBenefitsSnapshot(
                accountId,
                partnerId,
                "PARTNER-040",
                "ACTIVE",
                "CAT-2026-08",
                "BEAUTY_PARTNER",
                new BigDecimal("240.00"),
                new BigDecimal("80.00"),
                new BigDecimal("160.00"),
                List.of(
                        new BenefitGrantResponse(welcomeId, "WELCOME", "AVAILABLE", "CAT-2026-08", "2026-05-18T20:59:59Z", "CHECKOUT", "STR_MNEMO_PARTNER_BENEFITS_AVAILABLE", List.of(new BenefitProgressResponse("FIRST_ORDER_AMOUNT", new BigDecimal("3000.00"), new BigDecimal("3200.00"), "COMPLETED"))),
                        new BenefitGrantResponse(freeDeliveryId, "FREE_DELIVERY", "AVAILABLE", "CAT-2026-08", "2026-05-18T20:59:59Z", "CHECKOUT", "STR_MNEMO_PARTNER_BENEFITS_AVAILABLE", List.of(new BenefitProgressResponse("CART_AMOUNT", new BigDecimal("2500.00"), new BigDecimal("3200.00"), "COMPLETED"))),
                        new BenefitGrantResponse(UUID.fromString("00000000-0040-0000-0000-000000000013"), "CASHBACK", "PENDING", "CAT-2026-08", "2026-05-18T20:59:59Z", "WALLET", "STR_MNEMO_PARTNER_BENEFITS_CASHBACK_PENDING", List.of(new BenefitProgressResponse("PAID_ORDER", BigDecimal.ONE, BigDecimal.ONE, "COMPLETED")))
                ),
                new ReferralLinkResponse("REF-CAT-2026-08", "https://bestorigin.test/r/REF-CAT-2026-08", "QR:REF-CAT-2026-08", "REF-CAT-2026-08", "ACTIVE", "2026-05-18T20:59:59Z"),
                List.of(new ReferralEventResponse("***040@example.test", "QUALIFIED", "ORDER-040-REF", null, "2026-04-28T10:00:00Z", "CORR-040-REF")),
                List.of(new RewardItemResponse(rewardId, "REWARD-SKINCARE-BOX", "partnerBenefits.reward.skincareBox", "AVAILABLE", new BigDecimal("120.00"), 12, "STR_MNEMO_PARTNER_BENEFITS_REWARD_AVAILABLE")),
                List.of(new RetentionOfferResponse("RETENTION-040-ACTIVITY", "PARTNER_ACTIVITY_RISK", "CATALOG_ACTIVITY_DROP", "AVAILABLE", "2026-05-18T20:59:59Z")),
                List.of(
                        new SupportTimelineEventResponse("BENEFIT:" + welcomeId, "BENEFIT_GRANTED", "WELCOME_PROGRAM", "partner-benefits", "partner-benefits", "STR_MNEMO_PARTNER_BENEFITS_AVAILABLE", "2026-04-28T08:00:00Z", "CORR-040-WELCOME"),
                        new SupportTimelineEventResponse("REFERRAL:REF-CAT-2026-08", "REFERRAL_QUALIFIED", "FIRST_ORDER", "admin-referral", "partner-benefits", "STR_MNEMO_PARTNER_BENEFITS_REFERRAL_REWARDED", "2026-04-28T10:00:00Z", "CORR-040-REF")
                )
        );
    }
}
