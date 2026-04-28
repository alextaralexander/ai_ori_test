// AUTO-GENERATED from agents/tests/. Do not edit this synchronized runtime copy manually.
package com.bestorigin.tests.feature041;

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
    void adminCreatesDryRunsAndPublishesBenefitProgram() throws Exception {
        String token = extractJsonString(loginAs("admin-benefit-program-manager").body(), "token", "test-token-admin-benefit-program-manager");

        String createBody = """
                {
                  "code":"CAT-2026-08-CASHBACK",
                  "type":"CASHBACK",
                  "catalogId":"CAT-2026-08",
                  "activeFrom":"2026-04-28T00:00:00Z",
                  "activeTo":"2026-05-18T23:59:59Z",
                  "ownerRole":"CRM",
                  "rules":{"cashbackModel":"PERCENT","rate":7.5,"currency":"RUB","orderLimit":1000.00,"userLimit":3000.00},
                  "eligibility":{"roles":["BEAUTY_PARTNER","BUSINESS_PARTNER"],"minOrderAmount":3000.00},
                  "compatibility":{"priority":40,"stackable":false,"maxBenefitsPerOrder":1},
                  "lifecycle":{"expiration":"CATALOG_END","gracePeriodDays":2}
                }
                """;
        HttpResponse<String> created = sendAuthorized("/api/admin/benefit-programs/programs", token, "POST", createBody, "ABP-041-CREATE");
        assertEquals(201, created.statusCode());
        assertContains(created.body(), "CAT-2026-08-CASHBACK");
        String programId = extractJsonString(created.body(), "id", "00000000-0041-0000-0000-000000000001");

        String dryRunBody = """
                {
                  "partnerNumber":"PARTNER-041",
                  "catalogId":"CAT-2026-08",
                  "cartId":"CART-041-001",
                  "scenario":"CHECKOUT"
                }
                """;
        HttpResponse<String> dryRun = sendAuthorized("/api/admin/benefit-programs/programs/" + programId + "/dry-run", token, "POST", dryRunBody, "ABP-041-DRY-RUN");
        assertEquals(200, dryRun.statusCode());
        assertContains(dryRun.body(), "applicable");
        assertContains(dryRun.body(), "correlationId");

        String statusBody = "{\"targetStatus\":\"SCHEDULED\",\"reasonCode\":\"CATALOG_2026_08_APPROVED\",\"scheduledAt\":\"2026-04-28T00:00:00Z\"}";
        HttpResponse<String> scheduled = sendAuthorized("/api/admin/benefit-programs/programs/" + programId + "/status", token, "POST", statusBody, "ABP-041-SCHEDULE");
        assertEquals(200, scheduled.statusCode());
        assertContains(scheduled.body(), "SCHEDULED");
    }

    @Test
    void financeApprovesBudgetAndCreatesManualAdjustment() throws Exception {
        String token = extractJsonString(loginAs("admin-benefit-program-finance").body(), "token", "test-token-admin-benefit-program-finance");
        String programId = "00000000-0041-0000-0000-000000000001";

        String budgetBody = """
                {
                  "currency":"RUB",
                  "totalBudget":500000.00,
                  "cashbackLimit":3000.00,
                  "discountLimit":1500.00,
                  "redemptionLimit":1000,
                  "stopOnExhausted":true
                }
                """;
        HttpResponse<String> budget = sendAuthorized("/api/admin/benefit-programs/programs/" + programId + "/budgets", token, "PUT", budgetBody, "ABP-041-BUDGET");
        assertEquals(200, budget.statusCode());
        assertContains(budget.body(), "RUB");

        String adjustmentBody = """
                {
                  "targetPartnerNumber":"PARTNER-041",
                  "adjustmentType":"CASHBACK",
                  "amount":500.00,
                  "currency":"RUB",
                  "reasonCode":"SUPPORT_APPROVED_CORRECTION",
                  "evidenceRef":"CASE-041-001"
                }
                """;
        HttpResponse<String> adjustment = sendAuthorized("/api/admin/benefit-programs/programs/" + programId + "/manual-adjustments", token, "POST", adjustmentBody, "ABP-041-ADJUST");
        assertEquals(201, adjustment.statusCode());
        assertContains(adjustment.body(), "APPROVED");
    }

    @Test
    void auditAndRbacErrorsUseMnemonics() throws Exception {
        String auditorToken = extractJsonString(loginAs("admin-benefit-program-auditor").body(), "token", "test-token-admin-benefit-program-auditor");
        String programId = "00000000-0041-0000-0000-000000000001";

        HttpResponse<String> audit = getAuthorized("/api/admin/benefit-programs/programs/" + programId + "/audit-events?actionCode=DRY_RUN", auditorToken);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");

        HttpResponse<String> integration = getAuthorized("/api/admin/benefit-programs/programs/" + programId + "/integration-events?targetContext=PARTNER_BENEFITS", auditorToken);
        assertEquals(200, integration.statusCode());
        assertContains(integration.body(), "idempotencyKey");

        String supportToken = extractJsonString(loginAs("partner-support").body(), "token", "test-token-partner-support");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/benefit-programs/programs", supportToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        adminCreatesDryRunsAndPublishesBenefitProgram();
        financeApprovesBudgetAndCreatesManualAdjustment();
        auditAndRbacErrorsUseMnemonics();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "@bestorigin.test\",\"password\":\"test-password\",\"role\":\"" + role + "\"}";
        return send("/api/auth/login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", "", token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException {
        return send(path, method, body, token, idempotencyKey);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        if ("GET".equals(method)) {
            builder.GET();
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
        return valueEnd < 0 ? fallback : body.substring(valueStart, valueEnd);
    }
}
