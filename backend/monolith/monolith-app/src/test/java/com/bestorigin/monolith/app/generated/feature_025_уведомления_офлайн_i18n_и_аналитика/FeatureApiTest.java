// GENERATED FROM agents/tests/api/feature_025_уведомления_офлайн_i18n_и_аналитика/FeatureApiTest.java - DO NOT EDIT MANUALLY.
package com.bestorigin.tests.feature025;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class FeatureApiTest {

    private static final String BASE_URL = System.getProperty("bestorigin.baseUrl", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void customerLoginLoadsRuntimeConfigAndConsentPreferences() throws Exception {
        String token = extractJsonString(loginAs("customer").body(), "token", "test-token-customer");

        HttpResponse<String> configResponse = getAuthorized("/api/platform-experience/runtime-config", token);
        assertEquals(200, configResponse.statusCode());
        assertContains(configResponse.body(), "platform-experience");
        assertContains(configResponse.body(), "analyticsChannels");
        assertContains(configResponse.body(), "consentPolicyVersion");
        assertContains(configResponse.body(), "STR_MNEMO_PLATFORM_EXPERIENCE_CONFIG_READY");
        assertNoHardcodedRussianUiText(configResponse.body());

        HttpResponse<String> consentResponse = getAuthorized("/api/platform-experience/consent/preferences?subjectUserId=USR-025-CUST&policyVersion=consent-2026-04", token);
        assertEquals(200, consentResponse.statusCode());
        assertContains(consentResponse.body(), "functionalAllowed");
        assertContains(consentResponse.body(), "analyticsAllowed");
        assertContains(consentResponse.body(), "marketingAllowed");
    }

    @Test
    void customerUpdatesConsentPreferencesWithMnemonicResponse() throws Exception {
        String token = extractJsonString(loginAs("customer").body(), "token", "test-token-customer");
        String body = "{\"subjectUserId\":\"USR-025-CUST\",\"subjectRole\":\"customer\",\"policyVersion\":\"consent-2026-04\",\"analyticsAllowed\":true,\"marketingAllowed\":false,\"sourceRoute\":\"/checkout\",\"version\":1}";

        HttpResponse<String> response = sendAuthorized("/api/platform-experience/consent/preferences", token, "PUT", body);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"analyticsAllowed\":true");
        assertContains(response.body(), "\"marketingAllowed\":false");
        assertContains(response.body(), "STR_MNEMO_PLATFORM_CONSENT_UPDATED");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void notificationPreferencesReturnCriticalNotificationContract() throws Exception {
        String token = extractJsonString(loginAs("partner").body(), "token", "test-token-partner");

        HttpResponse<String> response = getAuthorized("/api/platform-experience/notification/preferences?subjectUserId=USR-025-PARTNER&locale=ru-RU", token);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "toastEnabled");
        assertContains(response.body(), "modalEnabled");
        assertContains(response.body(), "offlinePopupEnabled");
        assertContains(response.body(), "criticalNotificationsRequired");
        assertContains(response.body(), "STR_MNEMO_PLATFORM_NOTIFICATION_PREFERENCES_READY");
    }

    @Test
    void analyticsDiagnosticAcceptsConsentDeniedWithoutPersonalData() throws Exception {
        String token = extractJsonString(loginAs("partner").body(), "token", "test-token-partner");
        String body = "{\"channelCode\":\"MINDBOX\",\"eventCode\":\"OFFLINE_SALE_CREATED\",\"eventStatus\":\"CONSENT_DENIED\",\"reasonCode\":\"CONSENT_DENIED\",\"sourceRoute\":\"/business/offline-sales\",\"subjectRole\":\"partner\",\"correlationId\":\"CORR-025-CONSENT\",\"occurredAt\":\"2026-04-27T12:00:00Z\"}";

        HttpResponse<String> response = sendAuthorized("/api/platform-experience/diagnostics/analytics-events", token, "POST", body);

        assertEquals(202, response.statusCode());
        assertContains(response.body(), "accepted");
        assertContains(response.body(), "STR_MNEMO_PLATFORM_DIAGNOSTIC_ACCEPTED");
        assertFalse(response.body().contains("password"));
        assertFalse(response.body().contains("token"));
        assertFalse(response.body().contains("paymentCard"));
    }

    @Test
    void i18nMissingKeyDiagnosticIsAccepted() throws Exception {
        String token = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");
        String body = "{\"i18nKey\":\"notifications.feature025.missing\",\"locale\":\"ru-RU\",\"sourceRoute\":\"/employee\",\"componentKey\":\"NotificationProvider\",\"environmentCode\":\"test\",\"correlationId\":\"CORR-025-I18N\",\"occurredAt\":\"2026-04-27T12:05:00Z\"}";

        HttpResponse<String> response = sendAuthorized("/api/platform-experience/diagnostics/i18n-missing-keys", token, "POST", body);

        assertEquals(202, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PLATFORM_DIAGNOSTIC_ACCEPTED");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void trackingAdminReadsDiagnosticsSummary() throws Exception {
        String token = extractJsonString(loginAs("tracking-admin").body(), "token", "test-token-tracking-admin");

        HttpResponse<String> response = getAuthorized("/api/platform-experience/diagnostics/summary?from=2026-04-27T00:00:00Z&to=2026-04-28T00:00:00Z&channelCode=YANDEX_METRIKA", token);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "channelSummaries");
        assertContains(response.body(), "YANDEX_METRIKA");
        assertContains(response.body(), "STR_MNEMO_PLATFORM_DIAGNOSTICS_READY");
    }

    @Test
    void customerCannotReadAnalyticsDiagnosticsSummary() throws Exception {
        String token = extractJsonString(loginAs("customer").body(), "token", "test-token-customer");

        HttpResponse<String> response = getAuthorized("/api/platform-experience/diagnostics/summary?from=2026-04-27T00:00:00Z&to=2026-04-28T00:00:00Z", token);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN");
        assertNoHardcodedRussianUiText(response.body());
    }

    public void assertFeatureGreenPath() throws Exception {
        customerLoginLoadsRuntimeConfigAndConsentPreferences();
        customerUpdatesConsentPreferencesWithMnemonicResponse();
        notificationPreferencesReturnCriticalNotificationContract();
        analyticsDiagnosticAcceptsConsentDeniedWithoutPersonalData();
        i18nMissingKeyDiagnosticIsAccepted();
        trackingAdminReadsDiagnosticsSummary();
        customerCannotReadAnalyticsDiagnosticsSummary();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body) throws IOException, InterruptedException {
        return send(path, method, body, token);
    }

    private HttpResponse<String> send(String path, String method, String body, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest request = builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Согласие обновлено"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Диагностика готова"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
    }

    private static String extractJsonString(String body, String field, String fallback) {
        String marker = "\"" + field + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback;
    }
}
