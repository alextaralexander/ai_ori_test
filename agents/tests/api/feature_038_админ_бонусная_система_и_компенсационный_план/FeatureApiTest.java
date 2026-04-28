package com.bestorigin.tests.feature038;

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
    void adminCreatesPreviewsAndActivatesBonusRule() throws Exception {
        String token = extractJsonString(loginAs("bonus-admin").body(), "token", "test-token-bonus-admin");
        String body = "{\"ruleCode\":\"ORDER_BONUS_038\",\"ruleType\":\"ORDER_BONUS\",\"priority\":38,\"currency\":\"RUB\",\"rateValue\":7.5,\"validFrom\":\"2026-04-01T00:00:00Z\",\"validTo\":\"2026-04-21T23:59:59Z\"}";
        HttpResponse<String> created = sendAuthorized("/api/admin/bonus-program/rules", token, "POST", body, "BONUS-038-RULE");
        assertEquals(201, created.statusCode());
        assertContains(created.body(), "ORDER_BONUS_038");
        String ruleId = extractJsonString(created.body(), "id", "00000000-0000-0000-0000-000000000038");

        String previewBody = "{\"testOrderId\":\"BO-E2E-038-1\",\"partnerId\":\"PTR-E2E-038-1\"}";
        HttpResponse<String> preview = sendAuthorized("/api/admin/bonus-program/rules/" + ruleId + "/preview", token, "POST", previewBody, "BONUS-038-PREVIEW");
        assertEquals(200, preview.statusCode());
        assertContains(preview.body(), "expectedAmount");

        HttpResponse<String> activated = sendAuthorized("/api/admin/bonus-program/rules/" + ruleId + "/activate", token, "POST", "{}", "BONUS-038-ACTIVATE");
        assertEquals(200, activated.statusCode());
        assertContains(activated.body(), "ACTIVE");
    }

    @Test
    void mlmManagerRunsCalculationAndFinanceSendsPayoutBatch() throws Exception {
        String mlmToken = extractJsonString(loginAs("mlm-manager").body(), "token", "test-token-mlm-manager");
        String calculationBody = "{\"periodCode\":\"2026-04\",\"recalculation\":true}";
        HttpResponse<String> calculation = sendAuthorized("/api/admin/bonus-program/calculations", mlmToken, "POST", calculationBody, "BONUS-038-CALC");
        assertEquals(202, calculation.statusCode());
        assertContains(calculation.body(), "correlationId");

        HttpResponse<String> accruals = getAuthorized("/api/admin/bonus-program/accruals?periodCode=2026-04&status=PAYOUT_READY", mlmToken);
        assertEquals(200, accruals.statusCode());
        assertContains(accruals.body(), "PAYOUT_READY");

        String financeToken = extractJsonString(loginAs("finance-manager").body(), "token", "test-token-finance-manager");
        String batchBody = "{\"periodCode\":\"2026-04\",\"currency\":\"RUB\",\"regionCode\":\"RU-MSK\",\"partnerSegment\":\"LEADER\"}";
        HttpResponse<String> batch = sendAuthorized("/api/admin/bonus-program/payout-batches", financeToken, "POST", batchBody, "BONUS-038-BATCH");
        assertEquals(201, batch.statusCode());
        String batchId = extractJsonString(batch.body(), "id", "00000000-0000-0000-0000-000000000138");

        HttpResponse<String> approved = sendAuthorized("/api/admin/bonus-program/payout-batches/" + batchId + "/approve", financeToken, "POST", "{}", "BONUS-038-APPROVE");
        assertEquals(200, approved.statusCode());
        assertContains(approved.body(), "APPROVED");

        HttpResponse<String> sent = sendAuthorized("/api/admin/bonus-program/payout-batches/" + batchId + "/send", financeToken, "POST", "{}", "BONUS-038-SEND");
        assertEquals(202, sent.statusCode());
        assertContains(sent.body(), "SENT");
    }

    public void assertFeatureGreenPath() throws Exception {
        adminCreatesPreviewsAndActivatesBonusRule();
        mlmManagerRunsCalculationAndFinanceSendsPayoutBatch();
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