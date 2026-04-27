/* Managed synchronized artifact from agents/tests/api. Do not edit manually. */
package com.bestorigin.tests.feature036;

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
    void businessAdminReadsKpiDashboardAndAlerts() throws Exception {
        String token = extractJsonString(loginAs("business-admin").body(), "token", "test-token-business-admin");
        HttpResponse<String> dashboard = getAuthorized("/api/admin/platform/kpis?period=campaign-2026-05&region=RU-MSK", token);
        assertEquals(200, dashboard.statusCode());
        assertContains(dashboard.body(), "GMV");
        assertContains(dashboard.body(), "FULFILLMENT_SLA");
        assertContains(dashboard.body(), "STR_MNEMO_ADMIN_PLATFORM_KPI_READY");
        HttpResponse<String> alerts = getAuthorized("/api/admin/platform/alerts", token);
        assertEquals(200, alerts.statusCode());
        assertContains(alerts.body(), "STALE_KPI_SOURCE");
    }

    @Test
    void integrationAdminUpdatesIntegrationSettings() throws Exception {
        String token = extractJsonString(loginAs("integration-admin").body(), "token", "test-token-integration-admin");
        HttpResponse<String> statuses = getAuthorized("/api/admin/platform/integrations", token);
        assertEquals(200, statuses.statusCode());
        assertContains(statuses.body(), "WMS_1C");
        assertFalse(statuses.body().contains("secret"));
        String body = "{\"slaMinutes\":15,\"retryPolicy\":\"EXPONENTIAL_3\",\"maintenanceWindow\":\"SUN 02:00-03:00\",\"reasonCode\":\"SLA_TUNING\"}";
        HttpResponse<String> saved = sendAuthorized("/api/admin/platform/integrations/WMS_1C", token, "PUT", body, "PLATFORM-036-WMS");
        assertEquals(200, saved.statusCode());
        assertContains(saved.body(), "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED");
        assertContains(saved.body(), "CORR-036-INTEGRATION");
    }

    @Test
    void auditAdminSearchesAuditEvents() throws Exception {
        String token = extractJsonString(loginAs("audit-admin").body(), "token", "test-token-audit-admin");
        HttpResponse<String> audit = getAuthorized("/api/admin/platform/audit-events?domain=INTEGRATION&actionCode=INTEGRATION_SETTINGS_SAVED", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertContains(audit.body(), "maskedSubjectRef");
        assertNoHardcodedRussianUiText(audit.body());
    }

    @Test
    void reportExportRequiresValidFormatAndRole() throws Exception {
        String token = extractJsonString(loginAs("bi-analyst").body(), "token", "test-token-bi-analyst");
        HttpResponse<String> export = sendAuthorized("/api/admin/platform/reports/exports", token, "POST", "{\"reportType\":\"KPI_SLA\",\"format\":\"XLSX\",\"period\":\"campaign-2026-05\"}", "PLATFORM-036-EXPORT");
        assertEquals(202, export.statusCode());
        assertContains(export.body(), "exportId");
        HttpResponse<String> invalid = sendAuthorized("/api/admin/platform/reports/exports", token, "POST", "{\"reportType\":\"KPI_SLA\",\"format\":\"HTML\",\"period\":\"campaign-2026-05\"}", "PLATFORM-036-EXPORT-BAD");
        assertEquals(400, invalid.statusCode());
        assertContains(invalid.body(), "STR_MNEMO_ADMIN_PLATFORM_EXPORT_INVALID");
    }

    @Test
    void forbiddenRoleReceivesMnemonicCode() throws Exception {
        String token = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        HttpResponse<String> response = getAuthorized("/api/admin/platform/kpis", token);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        businessAdminReadsKpiDashboardAndAlerts();
        integrationAdminUpdatesIntegrationSettings();
        auditAdminSearchesAuditEvents();
        reportExportRequiresValidFormatAndRole();
        forbiddenRoleReceivesMnemonicCode();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException { return send("/api/auth/test-login", "POST", "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}", null, null); }
    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException { return send(path, "GET", null, token, null); }
    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException { return send(path, method, body, token, idempotencyKey); }
    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path)).header("Accept", "application/json").header("Accept-Language", "ru-RU").header("Content-Type", "application/json; charset=utf-8");
        if (token != null) { builder.header("Authorization", "Bearer " + token); }
        if (idempotencyKey != null) { builder.header("Idempotency-Key", idempotencyKey); }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        return http.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
    private static void assertContains(String body, String expected) { assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body); }
    private static void assertNoHardcodedRussianUiText(String body) { assertFalse(body.contains("Интеграция сохранена")); assertFalse(body.contains("Доступ запрещен")); }
    private static String extractJsonString(String body, String field, String fallback) { String marker = "\"" + field + "\":\""; int start = body.indexOf(marker); if (start < 0) { return fallback; } int valueStart = start + marker.length(); int valueEnd = body.indexOf('"', valueStart); return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback; }
}