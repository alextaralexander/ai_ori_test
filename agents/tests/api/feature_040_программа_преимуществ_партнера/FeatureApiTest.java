package com.bestorigin.tests.feature040;

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
    void partnerSeesBenefitsAndAppliesCheckoutPreview() throws Exception {
        String token = extractJsonString(loginAs("partner").body(), "token", "test-token-partner");

        HttpResponse<String> summary = getAuthorized("/api/partner-benefits/me/summary?catalogId=CAT-2026-08", token);
        assertEquals(200, summary.statusCode());
        assertContains(summary.body(), "CAT-2026-08");
        assertContains(summary.body(), "WELCOME");
        assertContains(summary.body(), "FREE_DELIVERY");
        String benefitId = extractJsonString(summary.body(), "benefitId", "00000000-0040-0000-0000-000000000001");

        String previewBody = "{\"target\":\"CHECKOUT\",\"cartId\":\"CART-040-001\",\"checkoutId\":\"CHK-040-001\"}";
        HttpResponse<String> preview = sendAuthorized("/api/partner-benefits/me/benefits/" + benefitId + "/apply-preview", token, "POST", previewBody, "PB-040-PREVIEW");
        assertEquals(200, preview.statusCode());
        assertContains(preview.body(), "applicable");
    }

    @Test
    void partnerUsesReferralAndRewardShop() throws Exception {
        String token = extractJsonString(loginAs("partner").body(), "token", "test-token-partner");

        HttpResponse<String> referral = getAuthorized("/api/partner-benefits/me/referral-link", token);
        assertEquals(200, referral.statusCode());
        assertContains(referral.body(), "referralCode");
        assertContains(referral.body(), "REF-CAT-2026-08");

        HttpResponse<String> events = getAuthorized("/api/partner-benefits/me/referral-events?status=QUALIFIED", token);
        assertEquals(200, events.statusCode());
        assertContains(events.body(), "QUALIFIED");

        HttpResponse<String> rewards = getAuthorized("/api/partner-benefits/me/rewards?catalogId=CAT-2026-08&onlyAvailable=true", token);
        assertEquals(200, rewards.statusCode());
        assertContains(rewards.body(), "REWARD-SKINCARE-BOX");
        String rewardId = extractJsonString(rewards.body(), "rewardId", "00000000-0040-0000-0000-000000000101");

        HttpResponse<String> redemption = sendAuthorized("/api/partner-benefits/me/rewards/" + rewardId + "/redemptions", token, "POST", "{\"expectedCostPoints\":120.00}", "PB-040-REDEEM");
        assertEquals(201, redemption.statusCode());
        assertContains(redemption.body(), "RESERVED");
    }

    @Test
    void supportSeesTimelineAndForbiddenUserIsRejected() throws Exception {
        String supportToken = extractJsonString(loginAs("partner-support").body(), "token", "test-token-partner-support");
        HttpResponse<String> timeline = getAuthorized("/api/partner-benefits/support/accounts/PARTNER-040/timeline", supportToken);
        assertEquals(200, timeline.statusCode());
        assertContains(timeline.body(), "CORR-040");
        assertContains(timeline.body(), "STR_MNEMO_PARTNER_BENEFITS");

        String customerToken = extractJsonString(loginAs("customer").body(), "token", "test-token-customer");
        HttpResponse<String> forbidden = getAuthorized("/api/partner-benefits/me/summary?catalogId=CAT-2026-08", customerToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        partnerSeesBenefitsAndAppliesCheckoutPreview();
        partnerUsesReferralAndRewardShop();
        supportSeesTimelineAndForbiddenUserIsRejected();
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
