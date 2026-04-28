package com.bestorigin.monolith.partnerbenefits.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PartnerBenefitsDtos {

    private PartnerBenefitsDtos() {
    }

    public record MoneyAmount(String amount, String currency) {
        public static MoneyAmount rub(BigDecimal amount) {
            return new MoneyAmount(amount.toPlainString(), "RUB");
        }
    }

    public record PartnerBenefitsSummaryResponse(
            UUID accountId,
            UUID partnerId,
            String partnerNumber,
            String accountStatus,
            String catalogId,
            String currentTier,
            MoneyAmount rewardBalance,
            MoneyAmount cashbackPending,
            MoneyAmount cashbackConfirmed,
            List<BenefitGrantResponse> benefits,
            ReferralLinkResponse referralLink,
            List<RetentionOfferResponse> retentionOffers
    ) {
    }

    public record BenefitGrantResponse(
            UUID benefitId,
            String benefitType,
            String status,
            String catalogId,
            String expiresAt,
            String applicationTarget,
            String mnemonicCode,
            List<BenefitProgressResponse> progress
    ) {
    }

    public record BenefitProgressResponse(String conditionCode, BigDecimal requiredValue, BigDecimal currentValue, String progressStatus) {
    }

    public record BenefitApplyPreviewRequest(String target, String cartId, String checkoutId) {
    }

    public record BenefitApplyPreviewResponse(boolean applicable, UUID benefitId, String target, String mnemonicCode, MoneyAmount discountAmount) {
    }

    public record ReferralLinkResponse(String referralCode, String referralUrl, String qrPayload, String campaignId, String linkStatus, String expiresAt) {
    }

    public record ReferralEventPageResponse(List<ReferralEventResponse> items, int page, int size, long total) {
    }

    public record ReferralEventResponse(String maskedContact, String eventStatus, String qualifyingActionRef, String rejectionMnemonic, String occurredAt, String correlationId) {
    }

    public record RewardPageResponse(List<RewardItemResponse> items) {
    }

    public record RewardItemResponse(UUID rewardId, String rewardCode, String titleI18nKey, String rewardStatus, BigDecimal costPoints, int availableQuantity, String availabilityMnemonic) {
    }

    public record RewardRedemptionRequest(BigDecimal expectedCostPoints, UUID deliveryAddressId) {
    }

    public record RewardRedemptionResponse(UUID redemptionId, UUID rewardId, String redemptionStatus, BigDecimal costPoints, String correlationId) {
    }

    public record RetentionOfferResponse(String offerCode, String audienceCode, String riskReasonCode, String offerStatus, String expiresAt) {
    }

    public record SupportTimelineResponse(String partnerNumber, List<SupportTimelineEventResponse> events) {
    }

    public record SupportTimelineEventResponse(String subjectRef, String actionCode, String reasonCode, String actorRole, String sourceSystem, String mnemonicCode, String occurredAt, String correlationId) {
    }

    public record PartnerBenefitsErrorResponse(String code, String correlationId, List<PartnerBenefitsValidationReasonResponse> fieldErrors, Map<String, String> metadata) {
    }

    public record PartnerBenefitsValidationReasonResponse(String field, String code) {
    }
}
