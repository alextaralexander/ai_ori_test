package com.bestorigin.monolith.platformexperience.api;

import java.util.List;

public final class PlatformExperienceDtos {

    private PlatformExperienceDtos() {
    }

    public record PlatformRuntimeConfigResponse(
            String moduleKey,
            String environmentCode,
            String consentPolicyVersion,
            List<AnalyticsChannelConfigResponse> analyticsChannels,
            boolean diagnosticsEnabled,
            String messageCode
    ) {
    }

    public record AnalyticsChannelConfigResponse(
            String channelCode,
            boolean enabled,
            String consentCategory,
            boolean diagnosticsVisible
    ) {
    }

    public record ConsentPreferenceUpdateRequest(
            String subjectUserId,
            String subjectRole,
            String policyVersion,
            boolean analyticsAllowed,
            boolean marketingAllowed,
            String sourceRoute,
            int version
    ) {
    }

    public record ConsentPreferenceResponse(
            String subjectUserId,
            String subjectRole,
            String policyVersion,
            boolean functionalAllowed,
            boolean analyticsAllowed,
            boolean marketingAllowed,
            int version,
            String messageCode
    ) {
    }

    public record NotificationPreferenceResponse(
            String subjectUserId,
            String locale,
            boolean toastEnabled,
            boolean modalEnabled,
            boolean offlinePopupEnabled,
            boolean criticalNotificationsRequired,
            String messageCode
    ) {
    }

    public record AnalyticsDiagnosticEventRequest(
            String channelCode,
            String eventCode,
            String eventStatus,
            String reasonCode,
            String sourceRoute,
            String subjectRole,
            String correlationId,
            String occurredAt
    ) {
    }

    public record I18nMissingKeyEventRequest(
            String i18nKey,
            String locale,
            String sourceRoute,
            String componentKey,
            String environmentCode,
            String correlationId,
            String occurredAt
    ) {
    }

    public record DiagnosticAcceptedResponse(boolean accepted, String messageCode, String correlationId) {
    }

    public record AnalyticsDiagnosticsSummaryResponse(
            String from,
            String to,
            List<AnalyticsChannelSummaryResponse> channelSummaries,
            String messageCode
    ) {
    }

    public record AnalyticsChannelSummaryResponse(
            String channelCode,
            int sentCount,
            int skippedCount,
            int failedCount,
            String lastReasonCode
    ) {
    }

    public record PlatformExperienceErrorResponse(String messageCode, String correlationId) {
    }
}
