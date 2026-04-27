// GENERATED FROM agents\tests\api\feature_023_сотрудник_настройки_и_повышенные_права\FeatureApiTest.java - DO NOT EDIT MANUALLY.
package com.bestorigin.tests.feature023;

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
    void employeeLoadsProfileSettingsSummary() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/profile-settings", userContextId);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "employeeId");
        assertContains(response.body(), "sections");
        assertContains(response.body(), "general");
        assertContains(response.body(), "securityWarnings");
        assertContains(response.body(), "auditRecorded");
    }

    @Test
    void employeeUpdatesGeneralSettingsWithVersion() throws Exception {
        String userContextId = loginAs("employee-support");
        String body = "{\"displayName\":\"Employee 023\",\"jobTitle\":\"Support specialist\",\"departmentCode\":\"SUPPORT\",\"preferredLanguage\":\"ru\",\"timezone\":\"Europe/Moscow\",\"notificationChannel\":\"WORK_EMAIL\",\"version\":1}";

        HttpResponse<String> response = sendAuthorized("/api/employee/profile-settings/general", userContextId, "PUT", body);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "Employee 023");
        assertContains(response.body(), "Europe/Moscow");
        assertContains(response.body(), "version");
        assertContains(response.body(), "auditRecorded");
    }

    @Test
    void employeeReadsContactsAddressesDocumentsAndSecurity() throws Exception {
        String userContextId = loginAs("employee-support");

        assertContains(getAuthorized("/api/employee/profile-settings/contacts", userContextId).body(), "maskedValue");
        assertContains(getAuthorized("/api/employee/profile-settings/addresses", userContextId).body(), "addressType");
        assertContains(getAuthorized("/api/employee/profile-settings/documents", userContextId).body(), "fileReferenceId");
        String securityBody = getAuthorized("/api/employee/profile-settings/security", userContextId).body();
        assertContains(securityBody, "mfaEnabled");
        assertContains(securityBody, "recentEvents");
    }

    @Test
    void employeeCreatesElevatedRequestAndSupervisorApprovesIt() throws Exception {
        String employeeContextId = loginAs("employee-support");
        String supervisorContextId = loginAs("supervisor");
        String requestBody = "{\"policyCode\":\"EMPLOYEE_ELEVATED_SUPPORT_OPERATIONS\",\"reasonCode\":\"SUPPORT_ESCALATION\",\"reasonText\":\"Проверка проблемного заказа\",\"targetScope\":\"ORDER_SUPPORT\",\"requestedDurationMinutes\":20}";

        HttpResponse<String> requestResponse = sendAuthorized("/api/employee/super-user/requests", employeeContextId, "POST", requestBody);

        assertEquals(201, requestResponse.statusCode());
        assertContains(requestResponse.body(), "requestId");
        assertContains(requestResponse.body(), "PENDING_SUPERVISOR_APPROVAL");
        assertContains(requestResponse.body(), "auditRecorded");

        String requestId = extractJsonString(requestResponse.body(), "requestId", "00000000-0000-0000-0000-000000000023");
        HttpResponse<String> approveResponse = sendAuthorized("/api/employee/super-user/requests/" + requestId + "/approve", supervisorContextId, "POST", "{\"comment\":\"Проверено основание\"}");

        assertEquals(200, approveResponse.statusCode());
        assertContains(approveResponse.body(), "elevatedSessionId");
        assertContains(approveResponse.body(), "ACTIVE");
        assertContains(approveResponse.body(), "remainingSeconds");
    }

    @Test
    void employeeClosesElevatedSession() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = sendAuthorized("/api/employee/super-user/sessions/00000000-0000-0000-0000-000000000023/close", userContextId, "POST", "{}");

        assertEquals(204, response.statusCode());
    }

    @Test
    void invalidContactReturnsMnemonic() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = sendAuthorized("/api/employee/profile-settings/contacts", userContextId, "POST", "{\"contactType\":\"\",\"value\":\"\",\"version\":1}");

        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_CONTACT_INVALID");
    }

    @Test
    void deniedElevatedPolicyReturnsMnemonic() throws Exception {
        String userContextId = loginAs("employee-support");
        String requestBody = "{\"policyCode\":\"EMPLOYEE_ELEVATED_FINANCE_OVERRIDE\",\"reasonCode\":\"SUPPORT_ESCALATION\",\"reasonText\":\"Недопустимый scope\",\"targetScope\":\"FINANCE_OVERRIDE\",\"requestedDurationMinutes\":60}";

        HttpResponse<String> response = sendAuthorized("/api/employee/super-user/requests", userContextId, "POST", requestBody);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_ELEVATED_POLICY_DENIED");
    }

    @Test
    void guestCannotOpenProfileSettingsOrSuperUser() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> profileResponse = getAuthorized("/api/employee/profile-settings", userContextId);
        assertEquals(403, profileResponse.statusCode());
        assertContains(profileResponse.body(), "STR_MNEMO_EMPLOYEE_ACCESS_DENIED");

        HttpResponse<String> superUserResponse = getAuthorized("/api/employee/super-user", userContextId);
        assertEquals(403, superUserResponse.statusCode());
        assertContains(superUserResponse.body(), "STR_MNEMO_EMPLOYEE_SUPER_USER_FORBIDDEN");
    }

    public void assertFeatureGreenPath() throws Exception {
        employeeLoadsProfileSettingsSummary();
        employeeUpdatesGeneralSettingsWithVersion();
        employeeReadsContactsAddressesDocumentsAndSecurity();
        employeeCreatesElevatedRequestAndSupervisorApprovesIt();
        employeeClosesElevatedSession();
        invalidContactReturnsMnemonic();
        deniedElevatedPolicyReturnsMnemonic();
        guestCannotOpenProfileSettingsOrSuperUser();
    }

    private HttpResponse<String> getAuthorized(String path, String userContextId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> sendAuthorized(String path, String userContextId, String method, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + userContextId)
                .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
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
