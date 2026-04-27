package com.bestorigin.monolith.platformexperience.impl.service;

import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsDiagnosticEventRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsDiagnosticsSummaryResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.ConsentPreferenceResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.ConsentPreferenceUpdateRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.DiagnosticAcceptedResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.I18nMissingKeyEventRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.NotificationPreferenceResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.PlatformRuntimeConfigResponse;

public interface PlatformExperienceService {

    PlatformRuntimeConfigResponse runtimeConfig(String role);

    ConsentPreferenceResponse consentPreferences(String token, String subjectUserId, String policyVersion);

    ConsentPreferenceResponse updateConsentPreferences(String token, ConsentPreferenceUpdateRequest request);

    NotificationPreferenceResponse notificationPreferences(String token, String subjectUserId, String locale);

    DiagnosticAcceptedResponse recordAnalyticsDiagnostic(String token, AnalyticsDiagnosticEventRequest request);

    DiagnosticAcceptedResponse recordI18nMissingKey(String token, I18nMissingKeyEventRequest request);

    AnalyticsDiagnosticsSummaryResponse diagnosticsSummary(String token, String from, String to, String channelCode);
}
