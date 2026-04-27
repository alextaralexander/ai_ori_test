package com.bestorigin.tests.feature026;

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
    void superAdminCreatesInternalAccountAndReceivesMnemonicContract() throws Exception {
        String token = extractJsonString(loginAs("super-admin").body(), "token", "test-token-super-admin");
        String body = "{\"fullName\":\"Feature 026 Employee\",\"email\":\"employee026@bestorigin.test\",\"phone\":\"+70000000026\",\"department\":\"SUPPORT\",\"positionTitle\":\"Support operator\",\"accountType\":\"HUMAN\",\"status\":\"ACTIVE\"}";

        HttpResponse<String> response = sendAuthorized("/api/admin/rbac/accounts", token, "POST", body, "ELS-026-SUPER");

        assertEquals(201, response.statusCode());
        assertContains(response.body(), "employee026@bestorigin.test");
        assertContains(response.body(), "ACTIVE");
        assertFalse(response.body().contains("password"));
        assertFalse(response.body().contains("mfaSecret"));
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void duplicateInternalAccountEmailReturnsConflictMnemonic() throws Exception {
        String token = extractJsonString(loginAs("super-admin").body(), "token", "test-token-super-admin");
        String body = "{\"fullName\":\"Feature 026 Duplicate\",\"email\":\"employee026@bestorigin.test\",\"department\":\"SUPPORT\",\"positionTitle\":\"Support operator\",\"accountType\":\"HUMAN\",\"status\":\"ACTIVE\"}";

        HttpResponse<String> response = sendAuthorized("/api/admin/rbac/accounts", token, "POST", body, "ELS-026-SUPER");

        assertEquals(409, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_RBAC_ACCOUNT_ALREADY_EXISTS");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void superAdminPreviewsAndAssignsRolesWithEffectivePermissions() throws Exception {
        String token = extractJsonString(loginAs("super-admin").body(), "token", "test-token-super-admin");
        String accessBody = "{\"roleCodes\":[\"employee-support\"],\"permissionSetCodes\":[\"EMPLOYEE_SUPPORT_BASE\"],\"responsibilityScopes\":[{\"departmentId\":\"SUPPORT\"}]}";

        HttpResponse<String> preview = sendAuthorized("/api/admin/rbac/accounts/00000000-0000-0000-0000-000000000026/permission-preview", token, "POST", accessBody, null);
        assertEquals(200, preview.statusCode());
        assertContains(preview.body(), "effectivePermissions");
        assertContains(preview.body(), "requiredMfa");

        HttpResponse<String> assign = sendAuthorized("/api/admin/rbac/accounts/00000000-0000-0000-0000-000000000026/roles", token, "PUT", accessBody, "ELS-026-SUPER");
        assertEquals(200, assign.statusCode());
        assertContains(assign.body(), "EMPLOYEE_SUPPORT_BASE");
        assertNoHardcodedRussianUiText(assign.body());
    }

    @Test
    void hrAdminCannotAssignPermissionOutsideScope() throws Exception {
        String token = extractJsonString(loginAs("hr-admin").body(), "token", "test-token-hr-admin");
        String body = "{\"roleCodes\":[\"super-admin\"],\"permissionSetCodes\":[\"ADMIN_RBAC_FULL\"],\"responsibilityScopes\":[{\"departmentId\":\"ADMIN\"}]}";

        HttpResponse<String> response = sendAuthorized("/api/admin/rbac/accounts/00000000-0000-0000-0000-000000000026/roles", token, "PUT", body, null);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void securityAdminUpdatesPolicyAndRotatesServiceAccountSecret() throws Exception {
        String token = extractJsonString(loginAs("security-admin").body(), "token", "test-token-security-admin");
        String policyBody = "{\"policyType\":\"MFA\",\"policyCode\":\"ADMIN_RBAC_HIGH_RISK\",\"settings\":{\"required\":true,\"allowedMethods\":[\"TOTP\"]},\"version\":1}";

        HttpResponse<String> policy = sendAuthorized("/api/admin/rbac/security-policies", token, "PUT", policyBody, "ELS-026-SEC");
        assertEquals(200, policy.statusCode());
        assertContains(policy.body(), "auditRecorded");

        HttpResponse<String> rotated = sendAuthorized("/api/admin/rbac/service-accounts/10000000-0000-0000-0000-000000000026/rotate-secret", token, "POST", "{}", "ELS-026-SEC");
        assertEquals(200, rotated.statusCode());
        assertContains(rotated.body(), "oneTimeSecret");
        assertContains(rotated.body(), "maskedSecretHint");
        assertNoHardcodedRussianUiText(rotated.body());
    }

    @Test
    void auditorSearchesAuditTrailWithoutSecrets() throws Exception {
        String token = extractJsonString(loginAs("auditor").body(), "token", "test-token-auditor");

        HttpResponse<String> response = getAuthorized("/api/admin/rbac/audit-events?targetUserId=00000000-0000-0000-0000-000000000026&actionCode=ADMIN_ROLE_ASSIGNED", token);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "actionCode");
        assertContains(response.body(), "correlationId");
        assertFalse(response.body().contains("oneTimeSecret"));
        assertFalse(response.body().contains("sessionToken"));
    }

    @Test
    void employeeWithoutAdminRbacPermissionIsForbidden() throws Exception {
        String token = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");

        HttpResponse<String> response = getAuthorized("/api/admin/rbac/accounts", token);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED");
        assertNoHardcodedRussianUiText(response.body());
    }

    public void assertFeatureGreenPath() throws Exception {
        superAdminCreatesInternalAccountAndReceivesMnemonicContract();
        duplicateInternalAccountEmailReturnsConflictMnemonic();
        superAdminPreviewsAndAssignsRolesWithEffectivePermissions();
        hrAdminCannotAssignPermissionOutsideScope();
        securityAdminUpdatesPolicyAndRotatesServiceAccountSecret();
        auditorSearchesAuditTrailWithoutSecrets();
        employeeWithoutAdminRbacPermissionIsForbidden();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String elevatedSessionId) throws IOException, InterruptedException {
        return send(path, method, body, token, elevatedSessionId);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String elevatedSessionId) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (elevatedSessionId != null) {
            builder.header("X-Elevated-Session-Id", elevatedSessionId);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = builder.method(method, publisher).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Доступ запрещен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Учетная запись уже существует"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
