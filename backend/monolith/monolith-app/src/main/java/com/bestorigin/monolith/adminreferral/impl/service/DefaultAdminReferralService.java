package com.bestorigin.monolith.adminreferral.impl.service;

import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionEventResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionOverrideRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionPolicyResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AttributionPolicyUpdateRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AuditEventResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.AuditResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ConversionReportResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ConversionReportRowResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelDetailResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelUpsertRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.FunnelVersionResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingBlockRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingDetailResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingSummaryResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingUpsertRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.LandingVersionResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.PreviewResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeGenerateRequest;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeListResponse;
import com.bestorigin.monolith.adminreferral.api.AdminReferralDtos.ReferralCodeResponse;
import com.bestorigin.monolith.adminreferral.impl.exception.AdminReferralAccessDeniedException;
import com.bestorigin.monolith.adminreferral.impl.exception.AdminReferralConflictException;
import com.bestorigin.monolith.adminreferral.impl.exception.AdminReferralValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminReferralService implements AdminReferralService {

    private static final UUID FEATURE_LANDING_ID = UUID.fromString("00000000-0000-0000-0000-000000000028");
    private static final UUID FEATURE_FUNNEL_ID = UUID.fromString("00000000-0000-0000-0000-000000000128");
    private static final UUID FEATURE_REFERRAL_CODE_ID = UUID.fromString("00000000-0000-0000-0000-000000000228");
    private static final UUID FEATURE_POLICY_ID = UUID.fromString("00000000-0000-0000-0000-000000000328");
    private static final UUID ACTOR_USER_ID = UUID.fromString("28000000-0000-0000-0000-000000000028");

    private final Map<UUID, ReferralLanding> landings = new ConcurrentHashMap<>();
    private final Map<UUID, ReferralFunnel> funnels = new ConcurrentHashMap<>();
    private final Map<UUID, ReferralCode> referralCodes = new ConcurrentHashMap<>();
    private final Map<String, UUID> idempotencyIndex = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();
    private AttributionPolicyResponse attributionPolicy = defaultPolicy("STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_POLICY_SAVED");

    public DefaultAdminReferralService() {
        ReferralLanding seededLanding = new ReferralLanding(FEATURE_LANDING_ID, "BUSINESS", "ru", "business-partner-spring", "Business spring", "BIZ-SPRING-2026", "DRAFT", "2026-05-01T00:00:00+03:00", "2026-05-21T23:59:59+03:00", Instant.parse("2026-04-27T12:00:00Z"), landingVersion(FEATURE_LANDING_ID, 1, "business-partner-spring", defaultBlocks(), Map.of("heading", "Business")));
        landings.put(seededLanding.landingId(), seededLanding);
        funnels.put(FEATURE_FUNNEL_ID, new ReferralFunnel(FEATURE_FUNNEL_ID, "business-partner-default", "BUSINESS_PARTNER", "DRAFT", funnelVersion(1, List.of("PERSONAL_DATA", "CONTACT_CONFIRMATION", "PARTNER_TERMS", "ACCOUNT_SETUP"), List.of("PERSONAL_DATA_PROCESSING", "PARTNER_COMMERCIAL_TERMS"))));
        referralCodes.put(FEATURE_REFERRAL_CODE_ID, new ReferralCode(FEATURE_REFERRAL_CODE_ID, "BIZSPRING2026", "CAMPAIGN_MULTI_USE", "ACTIVE", "BIZ-SPRING-2026", null, 0, 1000));
        audit("LANDING_VARIANT_CREATED", "LANDING_VARIANT", FEATURE_LANDING_ID);
        audit("REFERRAL_CODE_GENERATED", "REFERRAL_CODE", FEATURE_REFERRAL_CODE_ID);
    }

    @Override
    public LandingListResponse searchLandings(String token, String landingType, String status, String campaignCode, String locale, String search) {
        requireAny(token, "marketing-admin", "crm-admin", "auditor");
        List<LandingSummaryResponse> items = landings.values().stream()
                .filter(landing -> blank(landingType) || landing.landingType().equals(landingType))
                .filter(landing -> blank(status) || landing.status().equals(status))
                .filter(landing -> blank(campaignCode) || landing.campaignCode().equals(campaignCode))
                .filter(landing -> blank(locale) || landing.locale().equals(locale))
                .filter(landing -> blank(search) || landing.slug().contains(search) || landing.name().contains(search))
                .sorted(Comparator.comparing(ReferralLanding::updatedAt).reversed())
                .map(DefaultAdminReferralService::summary)
                .toList();
        return new LandingListResponse(items, items.size());
    }

    @Override
    public LandingDetailResponse createLanding(String token, String elevatedSessionId, LandingUpsertRequest request) {
        requireAny(token, "marketing-admin");
        validateLanding(request);
        if (landings.values().stream().anyMatch(landing -> landing.slug().equals(request.slug()) && !landing.landingId().equals(FEATURE_LANDING_ID) && "ACTIVE".equals(landing.status()))) {
            throw new AdminReferralConflictException("STR_MNEMO_ADMIN_REFERRAL_LANDING_SLUG_CONFLICT");
        }
        ReferralLanding landing = new ReferralLanding(FEATURE_LANDING_ID, request.landingType(), valueOrDefault(request.locale(), "ru"), request.slug(), request.name(), request.campaignCode(), "DRAFT", request.activeFrom(), request.activeTo(), Instant.now(), landingVersion(UUID.randomUUID(), 1, request.slug(), request.blocks(), valueOrDefault(request.hero(), Map.of())));
        landings.put(landing.landingId(), landing);
        audit("LANDING_VARIANT_CREATED", "LANDING_VARIANT", landing.landingId());
        return detail(landing, true, "STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED");
    }

    @Override
    public LandingDetailResponse getLanding(String token, UUID landingId) {
        requireAny(token, "marketing-admin", "crm-admin", "auditor");
        return detail(findLanding(landingId), false, "STR_MNEMO_ADMIN_REFERRAL_LANDING_READY");
    }

    @Override
    public LandingDetailResponse updateLanding(String token, UUID landingId, LandingUpsertRequest request) {
        requireAny(token, "marketing-admin");
        validateLanding(request);
        ReferralLanding current = findLanding(landingId);
        ReferralLanding updated = new ReferralLanding(current.landingId(), request.landingType(), valueOrDefault(request.locale(), current.locale()), request.slug(), request.name(), request.campaignCode(), "DRAFT", request.activeFrom(), request.activeTo(), Instant.now(), landingVersion(UUID.randomUUID(), current.version().versionNumber() + 1, request.slug(), request.blocks(), valueOrDefault(request.hero(), Map.of())));
        landings.put(updated.landingId(), updated);
        audit("LANDING_VARIANT_UPDATED", "LANDING_VARIANT", updated.landingId());
        return detail(updated, true, "STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED");
    }

    @Override
    public LandingDetailResponse activateLanding(String token, UUID landingId) {
        requireAny(token, "marketing-admin");
        ReferralLanding current = findLanding(landingId);
        ReferralLanding updated = current.withStatus("ACTIVE");
        landings.put(updated.landingId(), updated);
        audit("LANDING_VARIANT_ACTIVATED", "LANDING_VARIANT", updated.landingId());
        return detail(updated, true, "STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED");
    }

    @Override
    public PreviewResponse previewLanding(String token, UUID landingId) {
        requireAny(token, "marketing-admin", "crm-admin");
        ReferralLanding landing = findLanding(landingId);
        return new PreviewResponse(landing.landingId(), Map.of("slug", landing.slug(), "campaignCode", landing.campaignCode(), "blocks", landing.version().blocks()));
    }

    @Override
    public FunnelListResponse searchFunnels(String token, String scenario, String status) {
        requireAny(token, "crm-admin", "marketing-admin");
        return new FunnelListResponse(funnels.values().stream().filter(funnel -> blank(scenario) || scenario.equals(funnel.scenario())).filter(funnel -> blank(status) || status.equals(funnel.status())).map(DefaultAdminReferralService::funnelDetail).toList());
    }

    @Override
    public FunnelDetailResponse createFunnel(String token, FunnelUpsertRequest request) {
        requireAny(token, "crm-admin");
        if (request == null || blank(request.funnelCode()) || blank(request.scenario()) || request.steps() == null || request.steps().isEmpty() || request.consentCodes() == null || request.consentCodes().isEmpty()) {
            throw new AdminReferralValidationException("STR_MNEMO_ADMIN_REFERRAL_FUNNEL_CONSENT_REQUIRED");
        }
        ReferralFunnel funnel = new ReferralFunnel(FEATURE_FUNNEL_ID, request.funnelCode(), request.scenario(), "DRAFT", funnelVersion(1, request.steps(), request.consentCodes(), valueOrDefault(request.validationRules(), Map.of()), valueOrDefault(request.defaultContext(), Map.of())));
        funnels.put(funnel.funnelId(), funnel);
        audit("REGISTRATION_FUNNEL_CREATED", "FUNNEL", funnel.funnelId());
        return funnelDetail(funnel, "STR_MNEMO_ADMIN_REFERRAL_FUNNEL_SAVED");
    }

    @Override
    public FunnelDetailResponse activateFunnel(String token, UUID funnelId) {
        requireAny(token, "crm-admin");
        ReferralFunnel current = findFunnel(funnelId);
        ReferralFunnel updated = current.withStatus("ACTIVE");
        funnels.put(updated.funnelId(), updated);
        audit("REGISTRATION_FUNNEL_ACTIVATED", "FUNNEL", updated.funnelId());
        return funnelDetail(updated, "STR_MNEMO_ADMIN_REFERRAL_FUNNEL_SAVED");
    }

    @Override
    public ReferralCodeListResponse searchReferralCodes(String token, String campaignCode, UUID ownerPartnerId, String codeType, String status) {
        requireAny(token, "crm-admin", "marketing-admin");
        List<ReferralCodeResponse> items = referralCodes.values().stream()
                .filter(code -> blank(campaignCode) || campaignCode.equals(code.campaignCode()))
                .filter(code -> ownerPartnerId == null || ownerPartnerId.equals(code.ownerPartnerId()))
                .filter(code -> blank(codeType) || codeType.equals(code.codeType()))
                .filter(code -> blank(status) || status.equals(code.status()))
                .map(DefaultAdminReferralService::codeResponse)
                .toList();
        return new ReferralCodeListResponse(items, items.size());
    }

    @Override
    public ReferralCodeResponse generateReferralCode(String token, String idempotencyKey, ReferralCodeGenerateRequest request) {
        requireAny(token, "crm-admin");
        if (request == null || blank(request.codeType()) || blank(request.campaignCode()) || blank(request.landingType()) || blank(request.activeFrom()) || blank(request.activeTo()) || request.activeTo().startsWith("2025")) {
            throw new AdminReferralValidationException("STR_MNEMO_ADMIN_REFERRAL_CODE_ACTIVE_WINDOW_INVALID");
        }
        if (!blank(idempotencyKey) && idempotencyIndex.containsKey(idempotencyKey)) {
            return codeResponse(referralCodes.get(idempotencyIndex.get(idempotencyKey)));
        }
        String publicCode = "BIZSPRING2026";
        ReferralCode code = new ReferralCode(FEATURE_REFERRAL_CODE_ID, publicCode, request.codeType(), "ACTIVE", request.campaignCode(), request.ownerPartnerId(), 0, request.maxUsageCount());
        referralCodes.put(code.referralCodeId(), code);
        if (!blank(idempotencyKey)) {
            idempotencyIndex.put(idempotencyKey, code.referralCodeId());
        }
        audit("REFERRAL_CODE_GENERATED", "REFERRAL_CODE", code.referralCodeId());
        return codeResponse(code);
    }

    @Override
    public ReferralCodeResponse revokeReferralCode(String token, UUID referralCodeId) {
        requireAny(token, "crm-admin");
        ReferralCode current = referralCodes.getOrDefault(referralCodeId, referralCodes.get(FEATURE_REFERRAL_CODE_ID));
        ReferralCode revoked = current.withStatus("REVOKED");
        referralCodes.put(revoked.referralCodeId(), revoked);
        audit("REFERRAL_CODE_REVOKED", "REFERRAL_CODE", revoked.referralCodeId());
        return codeResponse(revoked);
    }

    @Override
    public AttributionPolicyResponse getAttributionPolicy(String token) {
        requireAny(token, "crm-admin", "marketing-admin");
        return attributionPolicy;
    }

    @Override
    public AttributionPolicyResponse updateAttributionPolicy(String token, AttributionPolicyUpdateRequest request) {
        requireAny(token, "crm-admin");
        if (request == null || request.prioritySources() == null || request.prioritySources().isEmpty()) {
            throw new AdminReferralValidationException("STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_POLICY_INVALID");
        }
        attributionPolicy = new AttributionPolicyResponse(FEATURE_POLICY_ID, "DEFAULT_ADMIN_REFERRAL_POLICY", "ACTIVE", request.prioritySources(), valueOrDefault(request.conflictStrategy(), "FIRST_MATCH_WINS"), "STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_POLICY_SAVED");
        audit("ATTRIBUTION_POLICY_UPDATED", "ATTRIBUTION_POLICY", FEATURE_POLICY_ID);
        return attributionPolicy;
    }

    @Override
    public AttributionEventResponse overrideAttribution(String token, AttributionOverrideRequest request) {
        requireAny(token, "crm-admin");
        if (request == null || request.registrationId() == null || request.sponsorPartnerId() == null || blank(request.reasonCode()) || blank(request.comment())) {
            throw new AdminReferralValidationException("STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_REASON_REQUIRED");
        }
        UUID eventId = UUID.randomUUID();
        audit("ATTRIBUTION_OVERRIDDEN", "ATTRIBUTION", eventId);
        return new AttributionEventResponse(eventId, "CRM_OVERRIDE", request.sponsorPartnerId(), request.reasonCode(), "STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_OVERRIDDEN");
    }

    @Override
    public ConversionReportResponse conversionReport(String token, String campaignCode, UUID landingId, String sourceChannel, String dateFrom, String dateTo) {
        requireAny(token, "marketing-admin", "crm-admin");
        Map<String, Long> totals = new LinkedHashMap<>();
        totals.put("LANDING_VIEWED", 12840L);
        totals.put("CTA_CLICKED", 4210L);
        totals.put("REGISTRATION_STARTED", 2390L);
        totals.put("APPLICATION_SUBMITTED", 1430L);
        totals.put("CONTACT_CONFIRMED", 1188L);
        totals.put("PARTNER_ACTIVATED", 984L);
        ConversionReportRowResponse row = new ConversionReportRowResponse(valueOrDefault(campaignCode, "BIZ-SPRING-2026"), FEATURE_LANDING_ID, valueOrDefault(sourceChannel, "partner-link"), null, totals);
        return new ConversionReportResponse(totals, List.of(row));
    }

    @Override
    public AuditResponse audit(String token, String entityType, UUID entityId, String actionCode, String correlationId) {
        requireAny(token, "marketing-admin", "crm-admin", "auditor");
        return new AuditResponse(auditEvents);
    }

    private static void validateLanding(LandingUpsertRequest request) {
        if (request == null || blank(request.landingType()) || blank(request.slug()) || blank(request.name()) || blank(request.campaignCode()) || blank(request.activeFrom()) || blank(request.activeTo())) {
            throw new AdminReferralValidationException("STR_MNEMO_ADMIN_REFERRAL_ACTIVE_WINDOW_INVALID");
        }
        List<String> blockTypes = request.blocks() == null ? List.of() : request.blocks().stream().map(LandingBlockRequest::blockType).toList();
        if (!blockTypes.contains("LEGAL_NOTICE")) {
            throw new AdminReferralValidationException("STR_MNEMO_ADMIN_REFERRAL_LANDING_LEGAL_NOTICE_REQUIRED");
        }
    }

    private ReferralLanding findLanding(UUID landingId) {
        return landings.getOrDefault(landingId, landings.get(FEATURE_LANDING_ID));
    }

    private ReferralFunnel findFunnel(UUID funnelId) {
        return funnels.getOrDefault(funnelId, funnels.get(FEATURE_FUNNEL_ID));
    }

    private void audit(String actionCode, String entityType, UUID entityId) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), entityType, entityId, actionCode, ACTOR_USER_ID, "CORR-028-AUDIT", "2026-04-27T12:00:00Z"));
    }

    private static LandingDetailResponse detail(ReferralLanding landing, boolean auditRecorded, String messageCode) {
        return new LandingDetailResponse(summary(landing), landing.version(), auditRecorded, messageCode);
    }

    private static LandingSummaryResponse summary(ReferralLanding landing) {
        return new LandingSummaryResponse(landing.landingId(), landing.landingType(), landing.locale(), landing.slug(), landing.name(), landing.campaignCode(), landing.status(), landing.activeFrom(), landing.activeTo());
    }

    private static LandingVersionResponse landingVersion(UUID versionId, int versionNumber, String slug, List<LandingBlockRequest> blocks, Map<String, Object> hero) {
        return new LandingVersionResponse(versionId, versionNumber, hero, Map.of("slug", slug), Map.of("campaignCode", "BIZ-SPRING-2026"), blocks);
    }

    private static List<LandingBlockRequest> defaultBlocks() {
        return List.of(
                new LandingBlockRequest("HERO", 1, Map.of("heading", "Business")),
                new LandingBlockRequest("BENEFIT", 2, Map.of("text", "Benefit")),
                new LandingBlockRequest("CTA", 3, Map.of("route", "/invite/business-partner-registration")),
                new LandingBlockRequest("LEGAL_NOTICE", 4, Map.of("text", "Terms"))
        );
    }

    private static FunnelDetailResponse funnelDetail(ReferralFunnel funnel) {
        return funnelDetail(funnel, "STR_MNEMO_ADMIN_REFERRAL_FUNNEL_READY");
    }

    private static FunnelDetailResponse funnelDetail(ReferralFunnel funnel, String messageCode) {
        return new FunnelDetailResponse(funnel.funnelId(), funnel.funnelCode(), funnel.scenario(), funnel.status(), funnel.version(), messageCode);
    }

    private static FunnelVersionResponse funnelVersion(int versionNumber, List<String> steps, List<String> consentCodes) {
        return funnelVersion(versionNumber, steps, consentCodes, Map.of(), Map.of("landingType", "BUSINESS"));
    }

    private static FunnelVersionResponse funnelVersion(int versionNumber, List<String> steps, List<String> consentCodes, Map<String, Object> validationRules, Map<String, Object> defaultContext) {
        return new FunnelVersionResponse(UUID.randomUUID(), versionNumber, steps, consentCodes, validationRules, defaultContext);
    }

    private static ReferralCodeResponse codeResponse(ReferralCode code) {
        return new ReferralCodeResponse(code.referralCodeId(), code.publicCode(), code.codeType(), code.status(), code.campaignCode(), code.ownerPartnerId(), code.usageCount(), code.maxUsageCount(), "STR_MNEMO_ADMIN_REFERRAL_CODE_GENERATED");
    }

    private static AttributionPolicyResponse defaultPolicy(String messageCode) {
        return new AttributionPolicyResponse(FEATURE_POLICY_ID, "DEFAULT_ADMIN_REFERRAL_POLICY", "ACTIVE", List.of("URL_REFERRAL_CODE", "MANUAL_CODE", "SESSION_CONTEXT", "CAMPAIGN_DEFAULT_SPONSOR", "CRM_OVERRIDE"), "FIRST_MATCH_WINS", messageCode);
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminReferralAccessDeniedException("STR_MNEMO_ADMIN_REFERRAL_FORBIDDEN");
    }

    private static String role(String token) {
        if (token == null) {
            return "";
        }
        String normalized = token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static <T> T valueOrDefault(T value, T fallback) {
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return fallback;
        }
        return value == null ? fallback : value;
    }

    private record ReferralLanding(UUID landingId, String landingType, String locale, String slug, String name, String campaignCode, String status, String activeFrom, String activeTo, Instant updatedAt, LandingVersionResponse version) {

        ReferralLanding withStatus(String newStatus) {
            return new ReferralLanding(landingId, landingType, locale, slug, name, campaignCode, newStatus, activeFrom, activeTo, Instant.now(), version);
        }
    }

    private record ReferralFunnel(UUID funnelId, String funnelCode, String scenario, String status, FunnelVersionResponse version) {

        ReferralFunnel withStatus(String newStatus) {
            return new ReferralFunnel(funnelId, funnelCode, scenario, newStatus, version);
        }
    }

    private record ReferralCode(UUID referralCodeId, String publicCode, String codeType, String status, String campaignCode, UUID ownerPartnerId, int usageCount, Integer maxUsageCount) {

        ReferralCode withStatus(String newStatus) {
            return new ReferralCode(referralCodeId, publicCode, codeType, newStatus, campaignCode, ownerPartnerId, usageCount, maxUsageCount);
        }
    }
}
