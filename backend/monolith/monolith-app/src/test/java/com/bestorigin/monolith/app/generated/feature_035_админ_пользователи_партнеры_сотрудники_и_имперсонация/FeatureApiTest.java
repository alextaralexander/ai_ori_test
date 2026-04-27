/* Managed synchronized artifact from agents/tests/api. Do not edit manually. */
package com.bestorigin.tests.feature035;

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
    void masterDataAdminFindsSubjectAndChangesStatus() throws Exception {
        String token = extractJsonString(loginAs("master-data-admin").body(), "token", "test-token-master-data-admin");

        HttpResponse<String> search = getAuthorized("/api/admin/identity/subjects?query=USR-035-1001&subjectType=USER", token);
        assertEquals(200, search.statusCode());
        assertContains(search.body(), "USR-035-1001");
        assertFalse(search.body().contains("customer035@example.test"));

        String subjectId = extractJsonString(search.body(), "subjectId", "00000000-0000-0000-0000-000000000035");
        HttpResponse<String> card = getAuthorized("/api/admin/identity/subjects/" + subjectId, token);
        assertEquals(200, card.statusCode());
        assertContains(card.body(), "eligibilityRules");
        assertContains(card.body(), "auditPreview");
        assertFalse(card.body().contains("passportNumber"));

        String body = "{\"newStatus\":\"SUSPENDED\",\"reasonCode\":\"MANUAL_RISK_REVIEW\",\"comment\":\"risk-review\"}";
        HttpResponse<String> status = sendAuthorized("/api/admin/identity/subjects/" + subjectId + "/status", token, "POST", body, "IDENTITY-035-STATUS");
        assertEquals(200, status.statusCode());
        assertContains(status.body(), "SUSPENDED");
        assertContains(status.body(), "auditEventId");
    }

    @Test
    void invalidStatusTransitionReturnsMnemonicCode() throws Exception {
        String token = extractJsonString(loginAs("master-data-admin").body(), "token", "test-token-master-data-admin");
        String subjectId = "00000000-0000-0000-0000-000000000135";
        String body = "{\"newStatus\":\"ACTIVE\",\"reasonCode\":\"REACTIVATE_ARCHIVED\"}";

        HttpResponse<String> response = sendAuthorized("/api/admin/identity/subjects/" + subjectId + "/status", token, "POST", body, "IDENTITY-035-INVALID-STATUS");
        assertEquals(409, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_IDENTITY_INVALID_STATUS_TRANSITION");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void partnerSponsorTransferRejectsMlmCycle() throws Exception {
        String token = extractJsonString(loginAs("partner-ops-admin").body(), "token", "test-token-partner-ops-admin");
        String partnerSubjectId = "00000000-0000-0000-0000-000000000235";

        String validBody = "{\"newSponsorSubjectId\":\"00000000-0000-0000-0000-000000000335\",\"effectiveFrom\":\"2026-05-01T00:00:00Z\",\"reasonCode\":\"PARTNER_TRANSFER_APPROVED\"}";
        HttpResponse<String> valid = sendAuthorized("/api/admin/identity/partners/" + partnerSubjectId + "/sponsor-relationships", token, "POST", validBody, "IDENTITY-035-SPONSOR");
        assertEquals(200, valid.statusCode());
        assertContains(valid.body(), "impactPreview");

        String cycleBody = "{\"newSponsorSubjectId\":\"00000000-0000-0000-0000-000000000235\",\"effectiveFrom\":\"2026-05-01T00:00:00Z\",\"reasonCode\":\"INVALID_CYCLE\"}";
        HttpResponse<String> cycle = sendAuthorized("/api/admin/identity/partners/" + partnerSubjectId + "/sponsor-relationships", token, "POST", cycleBody, "IDENTITY-035-SPONSOR-CYCLE");
        assertEquals(409, cycle.statusCode());
        assertContains(cycle.body(), "STR_MNEMO_ADMIN_IDENTITY_ELIGIBILITY_CONFLICT");
    }

    @Test
    void employeeRoleConflictAndImpersonationAreControlled() throws Exception {
        String employeeAdminToken = extractJsonString(loginAs("employee-admin").body(), "token", "test-token-employee-admin");
        String employeeSubjectId = "00000000-0000-0000-0000-000000000435";
        String bindingsBody = "{\"reasonCode\":\"ROLE_ASSIGNMENT\",\"bindings\":[{\"roleCode\":\"FINANCE_OPERATOR\",\"operationalScope\":\"PAYMENTS\"},{\"roleCode\":\"REFUND_APPROVER\",\"operationalScope\":\"PAYMENTS\"}]}";
        HttpResponse<String> bindings = sendAuthorized("/api/admin/identity/employees/" + employeeSubjectId + "/bindings", employeeAdminToken, "PUT", bindingsBody, "IDENTITY-035-EMPLOYEE-CONFLICT");
        assertEquals(409, bindings.statusCode());
        assertContains(bindings.body(), "STR_MNEMO_ADMIN_IDENTITY_ROLE_CONFLICT");

        String securityToken = extractJsonString(loginAs("security-admin").body(), "token", "test-token-security-admin");
        String policyBody = "{\"policyCode\":\"SUPPORT_READ_ONLY\",\"actorRoleCode\":\"SUPPORT_LEAD\",\"targetSubjectType\":\"USER\",\"allowedActions\":[\"VIEW_PROFILE\"],\"forbiddenActions\":[\"PAYMENT\",\"BONUS_WITHDRAWAL\",\"PASSWORD_CHANGE\",\"PROFILE_EDIT\"],\"maxDurationMinutes\":30,\"approvalRequired\":false,\"reasonCode\":\"POLICY_UPDATE\"}";
        HttpResponse<String> policy = sendAuthorized("/api/admin/identity/impersonation/policies", securityToken, "POST", policyBody, "IDENTITY-035-POLICY");
        assertEquals(200, policy.statusCode());
        assertContains(policy.body(), "SUPPORT_READ_ONLY");

        String sessionBody = "{\"targetSubjectId\":\"00000000-0000-0000-0000-000000000535\",\"reasonCode\":\"SUPPORT_DIAGNOSTICS\",\"requestedDurationMinutes\":30}";
        HttpResponse<String> session = sendAuthorized("/api/admin/identity/impersonation/sessions", securityToken, "POST", sessionBody, "IDENTITY-035-SESSION");
        assertEquals(201, session.statusCode());
        assertContains(session.body(), "expiresAt");
    }

    @Test
    void auditSearchRequiresScopeAndMasksSensitiveValues() throws Exception {
        String auditToken = extractJsonString(loginAs("personal-data-auditor").body(), "token", "test-token-personal-data-auditor");
        HttpResponse<String> audit = getAuthorized("/api/admin/identity/audit-events?subjectId=00000000-0000-0000-0000-000000000035&actionCode=STATUS_CHANGED", auditToken);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("password"));
        assertFalse(audit.body().contains("fullEmail"));

        String forbiddenToken = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/identity/subjects", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION");
    }

    public void assertFeatureGreenPath() throws Exception {
        masterDataAdminFindsSubjectAndChangesStatus();
        invalidStatusTransitionReturnsMnemonicCode();
        partnerSponsorTransferRejectsMlmCycle();
        employeeRoleConflictAndImpersonationAreControlled();
        auditSearchRequiresScopeAndMasksSensitiveValues();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException {
        return send(path, method, body, token, idempotencyKey);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = builder.method(method, publisher).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Действие запрещено"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Статус недоступен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Имперсонация запрещена"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
