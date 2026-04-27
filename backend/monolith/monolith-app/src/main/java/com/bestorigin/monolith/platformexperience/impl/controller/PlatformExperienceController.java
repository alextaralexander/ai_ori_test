package com.bestorigin.monolith.platformexperience.impl.controller;

import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsDiagnosticEventRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.AnalyticsDiagnosticsSummaryResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.ConsentPreferenceResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.ConsentPreferenceUpdateRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.DiagnosticAcceptedResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.I18nMissingKeyEventRequest;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.NotificationPreferenceResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.PlatformExperienceErrorResponse;
import com.bestorigin.monolith.platformexperience.api.PlatformExperienceDtos.PlatformRuntimeConfigResponse;
import com.bestorigin.monolith.platformexperience.impl.exception.PlatformExperienceAccessDeniedException;
import com.bestorigin.monolith.platformexperience.impl.exception.PlatformExperienceValidationException;
import com.bestorigin.monolith.platformexperience.impl.service.PlatformExperienceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform-experience")
public class PlatformExperienceController {

    private final PlatformExperienceService service;

    public PlatformExperienceController(PlatformExperienceService service) {
        this.service = service;
    }

    @GetMapping("/runtime-config")
    public PlatformRuntimeConfigResponse runtimeConfig(@RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.runtimeConfig(role);
    }

    @GetMapping("/consent/preferences")
    public ConsentPreferenceResponse consentPreferences(
            @RequestHeader HttpHeaders headers,
            @RequestParam String subjectUserId,
            @RequestParam String policyVersion) {
        return service.consentPreferences(token(headers), subjectUserId, policyVersion);
    }

    @PutMapping("/consent/preferences")
    public ConsentPreferenceResponse updateConsentPreferences(@RequestHeader HttpHeaders headers, @RequestBody ConsentPreferenceUpdateRequest request) {
        return service.updateConsentPreferences(token(headers), request);
    }

    @GetMapping("/notification/preferences")
    public NotificationPreferenceResponse notificationPreferences(
            @RequestHeader HttpHeaders headers,
            @RequestParam String subjectUserId,
            @RequestParam String locale) {
        return service.notificationPreferences(token(headers), subjectUserId, locale);
    }

    @PostMapping("/diagnostics/analytics-events")
    public ResponseEntity<DiagnosticAcceptedResponse> recordAnalyticsDiagnostic(
            @RequestHeader HttpHeaders headers,
            @RequestBody AnalyticsDiagnosticEventRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.recordAnalyticsDiagnostic(token(headers), request));
    }

    @PostMapping("/diagnostics/i18n-missing-keys")
    public ResponseEntity<DiagnosticAcceptedResponse> recordI18nMissingKey(
            @RequestHeader HttpHeaders headers,
            @RequestBody I18nMissingKeyEventRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.recordI18nMissingKey(token(headers), request));
    }

    @GetMapping("/diagnostics/summary")
    public AnalyticsDiagnosticsSummaryResponse diagnosticsSummary(
            @RequestHeader HttpHeaders headers,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String channelCode) {
        return service.diagnosticsSummary(token(headers), from, to, channelCode);
    }

    @ExceptionHandler(PlatformExperienceAccessDeniedException.class)
    public ResponseEntity<PlatformExperienceErrorResponse> handleForbidden(PlatformExperienceAccessDeniedException ex) {
        HttpStatus status = "STR_MNEMO_AUTH_SESSION_EXPIRED".equals(ex.getMessage()) ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(error(ex.getMessage()));
    }

    @ExceptionHandler(PlatformExperienceValidationException.class)
    public ResponseEntity<PlatformExperienceErrorResponse> handleValidation(PlatformExperienceValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
    }

    private static PlatformExperienceErrorResponse error(String code) {
        return new PlatformExperienceErrorResponse(code, "CORR-025-ERROR");
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
