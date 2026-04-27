package com.bestorigin.monolith.adminreferral.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminReferralDtos {

    private AdminReferralDtos() {
    }

    public record AdminReferralErrorResponse(String messageCode, String correlationId) {
    }

    public record LandingListResponse(List<LandingSummaryResponse> items, long total) {
    }

    public record LandingSummaryResponse(
            UUID landingId,
            String landingType,
            String locale,
            String slug,
            String name,
            String campaignCode,
            String status,
            String activeFrom,
            String activeTo
    ) {
    }

    public record LandingDetailResponse(
            LandingSummaryResponse landing,
            LandingVersionResponse version,
            boolean auditRecorded,
            String messageCode
    ) {
    }

    public record LandingVersionResponse(
            UUID versionId,
            int versionNumber,
            Map<String, Object> hero,
            Map<String, Object> seo,
            Map<String, Object> campaignContext,
            List<LandingBlockRequest> blocks
    ) {
    }

    public record LandingBlockRequest(String blockType, int sortOrder, Map<String, Object> payload) {
    }

    public record LandingUpsertRequest(
            String landingType,
            String locale,
            String slug,
            String name,
            String campaignCode,
            String activeFrom,
            String activeTo,
            Map<String, Object> hero,
            Map<String, Object> seo,
            Map<String, Object> campaignContext,
            List<LandingBlockRequest> blocks
    ) {
    }

    public record PreviewResponse(UUID landingId, Map<String, Object> renderModel) {
    }

    public record FunnelListResponse(List<FunnelDetailResponse> items) {
    }

    public record FunnelDetailResponse(
            UUID funnelId,
            String funnelCode,
            String scenario,
            String status,
            FunnelVersionResponse version,
            String messageCode
    ) {
    }

    public record FunnelVersionResponse(
            UUID versionId,
            int versionNumber,
            List<String> steps,
            List<String> consentCodes,
            Map<String, Object> validationRules,
            Map<String, Object> defaultContext
    ) {
    }

    public record FunnelUpsertRequest(
            String funnelCode,
            String scenario,
            List<String> steps,
            List<String> consentCodes,
            Map<String, Object> validationRules,
            Map<String, Object> defaultContext
    ) {
    }

    public record ReferralCodeGenerateRequest(
            String codeType,
            String campaignCode,
            UUID ownerPartnerId,
            String landingType,
            String activeFrom,
            String activeTo,
            Integer maxUsageCount,
            Map<String, Object> constraints
    ) {
    }

    public record ReferralCodeResponse(
            UUID referralCodeId,
            String publicCode,
            String codeType,
            String status,
            String campaignCode,
            UUID ownerPartnerId,
            int usageCount,
            Integer maxUsageCount,
            String messageCode
    ) {
    }

    public record ReferralCodeListResponse(List<ReferralCodeResponse> items, long total) {
    }

    public record AttributionPolicyUpdateRequest(List<String> prioritySources, String conflictStrategy) {
    }

    public record AttributionPolicyResponse(
            UUID policyId,
            String policyCode,
            String status,
            List<String> prioritySources,
            String conflictStrategy,
            String messageCode
    ) {
    }

    public record AttributionOverrideRequest(UUID registrationId, UUID sponsorPartnerId, String reasonCode, String comment) {
    }

    public record AttributionEventResponse(
            UUID attributionEventId,
            String selectedSource,
            UUID sponsorPartnerId,
            String reasonCode,
            String messageCode
    ) {
    }

    public record ConversionReportResponse(Map<String, Long> totals, List<ConversionReportRowResponse> rows) {
    }

    public record ConversionReportRowResponse(
            String campaignCode,
            UUID landingId,
            String sourceChannel,
            UUID sponsorPartnerId,
            Map<String, Long> metrics
    ) {
    }

    public record AuditResponse(List<AuditEventResponse> items) {
    }

    public record AuditEventResponse(
            UUID auditEventId,
            String entityType,
            UUID entityId,
            String actionCode,
            UUID actorUserId,
            String correlationId,
            String occurredAt
    ) {
    }
}
