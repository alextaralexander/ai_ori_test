package com.bestorigin.tests.feature024;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void partnerLoginReturnsSessionContextAndDefaultRoute() throws Exception {
        HttpResponse<String> response = loginAs("partner", "INV-024-SPONSOR");

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "token");
        assertContains(response.body(), "defaultRoute");
        assertContains(response.body(), "/business");
        assertContains(response.body(), "routePolicies");
        assertContains(response.body(), "activePartner");
        assertContains(response.body(), "invitationCodeState");
    }

    @Test
    void currentSessionRestoresRolePolicies() throws Exception {
        String token = extractJsonString(loginAs("partner", null).body(), "token", "test-token-partner");

        HttpResponse<String> response = getAuthorized("/api/auth/session", token);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "partner");
        assertContains(response.body(), "/business");
        assertContains(response.body(), "auditRecorded");
    }

    @Test
    void routeAccessDeniesCustomerEmployeeRouteWithMnemonic() throws Exception {
        String token = extractJsonString(loginAs("customer", null).body(), "token", "test-token-customer");

        HttpResponse<String> response = sendAuthorized("/api/auth/session/route-access", token, "POST", "{\"route\":\"/employee\"}");

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_AUTH_ACCESS_DENIED");
    }

    @Test
    void invitationCodeIsStoredAndReturnedAsValidState() throws Exception {
        String token = extractJsonString(loginAs("guest", null).body(), "token", "test-token-guest");

        HttpResponse<String> response = sendAuthorized("/api/auth/invitation-code", token, "POST", "{\"invitationCode\":\"INV-024-SPONSOR\"}");

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "INV-024-SPONSOR");
        assertContains(response.body(), "VALID");
        assertContains(response.body(), "auditRecorded");
    }

    @Test
    void partnerSearchAndActivePartnerSwitchWorkInScope() throws Exception {
        String token = extractJsonString(loginAs("partner-leader", null).body(), "token", "test-token-partner-leader");

        HttpResponse<String> searchResponse = getAuthorized("/api/auth/partners/search?query=024", token);
        assertEquals(200, searchResponse.statusCode());
        assertContains(searchResponse.body(), "PART-024-001");
        assertContains(searchResponse.body(), "P-024-0001");

        HttpResponse<String> activeResponse = sendAuthorized("/api/auth/partners/active", token, "PUT", "{\"partnerId\":\"PART-024-001\"}");
        assertEquals(200, activeResponse.statusCode());
        assertContains(activeResponse.body(), "activePartner");
        assertContains(activeResponse.body(), "PART-024-001");
    }

    @Test
    void invalidPartnerSearchReturnsMnemonic() throws Exception {
        String token = extractJsonString(loginAs("partner-leader", null).body(), "token", "test-token-partner-leader");

        HttpResponse<String> response = getAuthorized("/api/auth/partners/search?query=P", token);

        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_AUTH_PARTNER_SEARCH_INVALID");
    }

    @Test
    void supervisorStartsAndFinishesImpersonation() throws Exception {
        String token = extractJsonString(loginAs("supervisor", null).body(), "token", "test-token-supervisor");
        String body = "{\"targetUserId\":\"USR-024-PARTNER\",\"targetRole\":\"partner\",\"reasonCode\":\"SUPPORT_CASE_REVIEW\",\"reasonText\":\"Проверка обращения партнера\",\"durationMinutes\":20}";

        HttpResponse<String> startResponse = sendAuthorized("/api/auth/impersonation", token, "POST", body);

        assertEquals(201, startResponse.statusCode());
        assertContains(startResponse.body(), "impersonationSessionId");
        assertContains(startResponse.body(), "USR-024-PARTNER");
        assertContains(startResponse.body(), "ACTIVE");

        String sessionId = extractJsonString(startResponse.body(), "impersonationSessionId", "02400000-0000-0000-0000-000000000004");
        HttpResponse<String> finishResponse = sendAuthorized("/api/auth/impersonation/" + sessionId + "/finish", token, "POST", "{}");
        assertEquals(200, finishResponse.statusCode());
        assertContains(finishResponse.body(), "supervisor");
        assertContains(finishResponse.body(), "auditRecorded");
    }

    @Test
    void employeeWithoutElevatedModeCannotImpersonate() throws Exception {
        String token = extractJsonString(loginAs("employee-support-no-elevated", null).body(), "token", "test-token-employee-support-no-elevated");
        String body = "{\"targetUserId\":\"USR-024-PARTNER\",\"targetRole\":\"partner\",\"reasonCode\":\"SUPPORT_CASE_REVIEW\",\"reasonText\":\"Нет elevated mode\",\"durationMinutes\":20}";

        HttpResponse<String> response = sendAuthorized("/api/auth/impersonation", token, "POST", body);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_AUTH_IMPERSONATION_FORBIDDEN");
    }

    @Test
    void logoutClearsSession() throws Exception {
        String token = extractJsonString(loginAs("partner", null).body(), "token", "test-token-partner");

        HttpResponse<String> response = sendAuthorized("/api/auth/session", token, "DELETE", "{}");

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "loggedOut");
        assertContains(response.body(), "auditRecorded");
    }

    public void assertFeatureGreenPath() throws Exception {
        partnerLoginReturnsSessionContextAndDefaultRoute();
        currentSessionRestoresRolePolicies();
        routeAccessDeniesCustomerEmployeeRouteWithMnemonic();
        invitationCodeIsStoredAndReturnedAsValidState();
        partnerSearchAndActivePartnerSwitchWorkInScope();
        invalidPartnerSearchReturnsMnemonic();
        supervisorStartsAndFinishesImpersonation();
        employeeWithoutElevatedModeCannotImpersonate();
        logoutClearsSession();
    }

    private HttpResponse<String> loginAs(String role, String invitationCode) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\""
                + (invitationCode == null ? "" : ",\"invitationCode\":\"" + invitationCode + "\"")
                + "}";
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
