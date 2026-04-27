package com.bestorigin.monolith.mlmstructure.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class MlmStructureDtos {

    private MlmStructureDtos() {
    }

    public record MlmVolume(BigDecimal amount, String currencyCode) {
    }

    public record MlmDashboardResponse(
            String campaignId,
            String leaderPersonNumber,
            MlmVolume personalVolume,
            MlmVolume groupVolume,
            int activePartnerCount,
            String currentRank,
            String nextRank,
            BigDecimal qualificationPercent,
            List<String> nextActions,
            String publicMnemo
    ) {
    }

    public record MlmPartnerNodeResponse(
            String personNumber,
            String displayName,
            String branchId,
            int structureLevel,
            String partnerRole,
            String partnerStatus,
            MlmVolume personalVolume,
            MlmVolume groupVolume,
            BigDecimal riskScore
    ) {
    }

    public record MlmCommunityResponse(
            String campaignId,
            List<MlmPartnerNodeResponse> partners,
            int totalElements,
            String publicMnemo
    ) {
    }

    public record MlmConversionFunnelResponse(
            String campaignId,
            int inviteSentCount,
            int inviteAcceptedCount,
            int registeredCount,
            int activatedCount,
            int firstOrderCount,
            BigDecimal conversionRatePercent,
            String publicMnemo
    ) {
    }

    public record MlmTeamActivityItemResponse(
            String personNumber,
            String activityType,
            String activityStatus,
            String occurredAt,
            boolean riskSignal,
            String publicMnemo
    ) {
    }

    public record MlmTeamActivityResponse(
            String campaignId,
            List<MlmTeamActivityItemResponse> items,
            int totalElements,
            String publicMnemo
    ) {
    }

    public record MlmUpgradeRequirementResponse(
            String code,
            String status,
            BigDecimal currentValue,
            BigDecimal targetValue,
            String publicMnemo
    ) {
    }

    public record MlmUpgradeResponse(
            String campaignId,
            String personNumber,
            String currentRank,
            String nextRank,
            BigDecimal qualificationProgress,
            String deadlineAt,
            List<MlmUpgradeRequirementResponse> requirements,
            String publicMnemo
    ) {
    }

    public record MlmPartnerCardResponse(
            String personNumber,
            String displayName,
            String sponsorPersonNumber,
            String branchId,
            int structureLevel,
            String partnerRole,
            String partnerStatus,
            MlmVolume personalVolume,
            MlmVolume groupVolume,
            MlmUpgradeResponse qualificationProgress,
            Map<String, String> linkedActions,
            String publicMnemo
    ) {
    }

    public record MlmStructureErrorResponse(
            String code,
            List<MlmStructureValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }

    public record MlmStructureValidationReasonResponse(String code, String severity, String target) {
    }
}
