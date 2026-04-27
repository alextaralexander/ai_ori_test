package com.bestorigin.monolith.platformexperience.impl.service;

import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsChannelConfigResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsChannelSummaryResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsDiagnosticEventRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsDiagnosticsSummaryResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.ConsentPreferenceResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.ConsentPreferenceUpdateRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.DiagnosticAcceptedResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.I18nMissingKeyEventRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.NotificationPreferenceResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.PlatformRuntimeConfigResponse;
import com.bestorigin.monolith.platformexperience.impl.exception.PlatformExperienceAccessDeniedException;
import com.bestorigin.monolith.platformexperience.impl.exception.PlatformExperienceValidationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultPlatformExperienceService implements PlatformExperienceService {

    private static final String POLICY_VERSION = "consent-2026-04";
    private static final String DIAGNOSTIC_ACCEPTED = "STR_MNEMO_PLATFORM_DIAGNOSTIC_ACCEPTED";
    private final Map<String, ConsentPreferenceResponse> consentPreferences = new ConcurrentHashMap<>();

    @Override
    public PlatformRuntimeConfigResponse runtimeConfig(String role) {
        boolean diagnosticsVisible = canReadDiagnostics(role);
        return new PlatformRuntimeConfigResponse(
                "platform-experience",
                "test",
                POLICY_VERSION,
                List.of(
                        new AnalyticsChannelConfigResponse("YANDEX_METRIKA", true, "analytics", diagnosticsVisible),
                        new AnalyticsChannelConfigResponse("MINDBOX", true, "marketing", diagnosticsVisible),
                        new AnalyticsChannelConfigResponse("HYBRID_PIXEL", true, "marketing", diagnosticsVisible)
                ),
                diagnosticsVisible,
                "STR_MNEMO_PLATFORM_EXPERIENCE_CONFIG_READY"
        );
    }

    @Override
    public ConsentPreferenceResponse consentPreferences(String token, String subjectUserId, String policyVersion) {
        requireAuthenticated(token);
        String key = consentKey(subjectUserId, policyVersion);
        return consentPreferences.computeIfAbsent(key, ignored -> new ConsentPreferenceResponse(
                blank(subjectUserId) ? userIdFromToken(token) : subjectUserId,
                roleFromToken(token),
                blank(policyVersion) ? POLICY_VERSION : policyVersion,
                true,
                false,
                false,
                1,
                "STR_MNEMO_PLATFORM_CONSENT_READY"
        ));
    }

    @Override
    public ConsentPreferenceResponse updateConsentPreferences(String token, ConsentPreferenceUpdateRequest request) {
        requireAuthenticated(token);
        if (request == null || blank(request.subjectUserId()) || blank(request.subjectRole()) || blank(request.policyVersion()) || blank(request.sourceRoute()) || request.version() < 1) {
            throw new PlatformExperienceValidationException("STR_MNEMO_PLATFORM_CONSENT_INVALID");
        }
        ConsentPreferenceResponse response = new ConsentPreferenceResponse(
                request.subjectUserId(),
                request.subjectRole(),
                request.policyVersion(),
                true,
                request.analyticsAllowed(),
                request.marketingAllowed(),
                request.version() + 1,
                "STR_MNEMO_PLATFORM_CONSENT_UPDATED"
        );
        consentPreferences.put(consentKey(request.subjectUserId(), request.policyVersion()), response);
        return response;
    }

    @Override
    public NotificationPreferenceResponse notificationPreferences(String token, String subjectUserId, String locale) {
        requireAuthenticated(token);
        return new NotificationPreferenceResponse(
                blank(subjectUserId) ? userIdFromToken(token) : subjectUserId,
                blank(locale) ? "ru-RU" : locale,
                true,
                true,
                true,
                true,
                "STR_MNEMO_PLATFORM_NOTIFICATION_PREFERENCES_READY"
        );
    }

    @Override
    public DiagnosticAcceptedResponse recordAnalyticsDiagnostic(String token, AnalyticsDiagnosticEventRequest request) {
        requireAuthenticated(token);
        if (request == null || blank(request.channelCode()) || blank(request.eventCode()) || blank(request.eventStatus()) || blank(request.sourceRoute()) || blank(request.subjectRole()) || blank(request.correlationId()) || blank(request.occurredAt())) {
            throw new PlatformExperienceValidationException("STR_MNEMO_PLATFORM_EXPERIENCE_VALIDATION_FAILED");
        }
        return new DiagnosticAcceptedResponse(true, DIAGNOSTIC_ACCEPTED, request.correlationId());
    }

    @Override
    public DiagnosticAcceptedResponse recordI18nMissingKey(String token, I18nMissingKeyEventRequest request) {
        requireAuthenticated(token);
        if (request == null || blank(request.i18nKey()) || blank(request.locale()) || blank(request.sourceRoute()) || blank(request.componentKey()) || blank(request.environmentCode()) || blank(request.correlationId()) || blank(request.occurredAt())) {
            throw new PlatformExperienceValidationException("STR_MNEMO_PLATFORM_EXPERIENCE_VALIDATION_FAILED");
        }
        return new DiagnosticAcceptedResponse(true, DIAGNOSTIC_ACCEPTED, request.correlationId());
    }

    @Override
    public AnalyticsDiagnosticsSummaryResponse diagnosticsSummary(String token, String from, String to, String channelCode) {
        requireAuthenticated(token);
        String role = roleFromToken(token);
        if (!canReadDiagnostics(role)) {
            throw new PlatformExperienceAccessDeniedException("STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN");
        }
        List<AnalyticsChannelSummaryResponse> summaries = List.of(
                summary("YANDEX_METRIKA", channelCode, "SENT"),
                summary("MINDBOX", channelCode, "CONSENT_DENIED"),
                summary("HYBRID_PIXEL", channelCode, "SENT")
        ).stream().filter(item -> blank(channelCode) || item.channelCode().equals(channelCode)).toList();
        return new AnalyticsDiagnosticsSummaryResponse(from, to, summaries, "STR_MNEMO_PLATFORM_DIAGNOSTICS_READY");
    }

    private static AnalyticsChannelSummaryResponse summary(String channelCode, String filter, String reasonCode) {
        int sent = "CONSENT_DENIED".equals(reasonCode) ? 0 : 8;
        int skipped = "CONSENT_DENIED".equals(reasonCode) ? 3 : 0;
        int failed = "ADAPTER_FAILED".equals(reasonCode) ? 1 : 0;
        return new AnalyticsChannelSummaryResponse(channelCode, sent, skipped, failed, reasonCode);
    }

    private static void requireAuthenticated(String token) {
        if (blank(normalizeToken(token))) {
            throw new PlatformExperienceAccessDeniedException("STR_MNEMO_AUTH_SESSION_EXPIRED");
        }
    }

    private static boolean canReadDiagnostics(String role) {
        return "tracking-admin".equals(role) || "admin".equals(role) || "supervisor".equals(role);
    }

    private static String consentKey(String subjectUserId, String policyVersion) {
        return (blank(subjectUserId) ? "anonymous" : subjectUserId) + ":" + (blank(policyVersion) ? POLICY_VERSION : policyVersion);
    }

    private static String userIdFromToken(String token) {
        String role = roleFromToken(token);
        return switch (role) {
            case "tracking-admin" -> "ADM-025-TRACKING";
            case "partner", "partner-leader" -> "USR-025-PARTNER";
            case "employee-support" -> "EMP-025-SUPPORT";
            case "customer" -> "USR-025-CUST";
            default -> role + "-user";
        };
    }

    private static String roleFromToken(String token) {
        String normalized = normalizeToken(token);
        if (normalized.startsWith("test-token-")) {
            return normalized.substring("test-token-".length());
        }
        return normalized;
    }

    private static String normalizeToken(String token) {
        return token == null ? "" : token.replace("Bearer ", "").trim();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
