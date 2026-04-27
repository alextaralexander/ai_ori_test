package com.bestorigin.monolith.adminreferral.impl.service;

import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionEventResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionOverrideRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionPolicyResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionPolicyUpdateRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AuditResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ConversionReportResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelDetailResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelUpsertRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingDetailResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingUpsertRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.PreviewResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeGenerateRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeResponse;
import java.util.UUID;

public interface AdminReferralService {

    LandingListResponse searchLandings(String token, String landingType, String status, String campaignCode, String locale, String search);

    LandingDetailResponse createLanding(String token, String elevatedSessionId, LandingUpsertRequest request);

    LandingDetailResponse getLanding(String token, UUID landingId);

    LandingDetailResponse updateLanding(String token, UUID landingId, LandingUpsertRequest request);

    LandingDetailResponse activateLanding(String token, UUID landingId);

    PreviewResponse previewLanding(String token, UUID landingId);

    FunnelListResponse searchFunnels(String token, String scenario, String status);

    FunnelDetailResponse createFunnel(String token, FunnelUpsertRequest request);

    FunnelDetailResponse activateFunnel(String token, UUID funnelId);

    ReferralCodeListResponse searchReferralCodes(String token, String campaignCode, UUID ownerPartnerId, String codeType, String status);

    ReferralCodeResponse generateReferralCode(String token, String idempotencyKey, ReferralCodeGenerateRequest request);

    ReferralCodeResponse revokeReferralCode(String token, UUID referralCodeId);

    AttributionPolicyResponse getAttributionPolicy(String token);

    AttributionPolicyResponse updateAttributionPolicy(String token, AttributionPolicyUpdateRequest request);

    AttributionEventResponse overrideAttribution(String token, AttributionOverrideRequest request);

    ConversionReportResponse conversionReport(String token, String campaignCode, UUID landingId, String sourceChannel, String dateFrom, String dateTo);

    AuditResponse audit(String token, String entityType, UUID entityId, String actionCode, String correlationId);
}
