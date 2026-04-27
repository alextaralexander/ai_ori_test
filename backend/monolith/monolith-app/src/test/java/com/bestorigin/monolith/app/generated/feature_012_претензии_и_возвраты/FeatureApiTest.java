// Synchronized from agents/tests/. Do not edit this generated runtime copy manually.
package com.bestorigin.tests.feature012;

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
    void customerCreatesClaimForDeliveredOrder() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"orderNumber\":\"ORD-011-MAIN\",\"reasonCode\":\"DAMAGED_ITEM\",\"requestedResolution\":\"REFUND\",\"comment\":\"photo attached\",\"items\":[{\"sku\":\"100-011\",\"quantity\":1}]}";

        HttpResponse<String> created = postAuthorized("/api/order/claims", body, userContextId, userContextId + "-claim-001");
        assertEquals(200, created.statusCode());
        assertContains(created.body(), "\"claimId\":\"CLM-012-001\"");
        assertContains(created.body(), "\"orderNumber\":\"ORD-011-MAIN\"");
        assertContains(created.body(), "\"status\":\"IN_REVIEW\"");
        assertContains(created.body(), "\"refundAmount\":3200.00");
        assertContains(created.body(), "STR_MNEMO_ORDER_CLAIM_CREATED");
    }

    @Test
    void customerSeesClaimHistoryAndDetails() throws Exception {
        String userContextId = loginAs("customer");
        createDefaultClaim(userContextId);

        HttpResponse<String> history = getAuthorized("/api/order/claims?query=CLM-012-001&status=IN_REVIEW", userContextId);
        assertEquals(200, history.statusCode());
        assertContains(history.body(), "\"claimId\":\"CLM-012-001\"");
        assertContains(history.body(), "\"orderNumber\":\"ORD-011-MAIN\"");
        assertContains(history.body(), "\"requestedResolution\":\"REFUND\"");

        HttpResponse<String> details = getAuthorized("/api/order/claims/CLM-012-001", userContextId);
        assertEquals(200, details.statusCode());
        assertContains(details.body(), "\"items\"");
        assertContains(details.body(), "\"events\"");
        assertContains(details.body(), "\"comments\"");
        assertContains(details.body(), "\"nextAction\":\"WAIT_SERVICE_DECISION\"");
        assertFalse(details.body().contains("storagePath"));
    }

    @Test
    void claimValidationRejectsUnavailableItem() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"orderNumber\":\"ORD-011-MAIN\",\"reasonCode\":\"DAMAGED_ITEM\",\"requestedResolution\":\"REFUND\",\"items\":[{\"sku\":\"GIFT-011\",\"quantity\":1}]}";

        HttpResponse<String> rejected = postAuthorized("/api/order/claims", body, userContextId, userContextId + "-claim-rejected");
        assertEquals(400, rejected.statusCode());
        assertContains(rejected.body(), "STR_MNEMO_ORDER_CLAIM_ITEM_UNAVAILABLE");
    }

    @Test
    void partnerSeesPartnerImpactForSupplementaryClaim() throws Exception {
        String userContextId = loginAs("partner");
        String body = "{\"orderNumber\":\"ORD-011-SUPP\",\"reasonCode\":\"MISSING_ITEM\",\"requestedResolution\":\"MISSING_ITEM\",\"items\":[{\"sku\":\"SUPP-011\",\"quantity\":1}]}";

        HttpResponse<String> created = postAuthorized("/api/order/claims", body, userContextId, userContextId + "-claim-supp");
        assertEquals(200, created.statusCode());
        assertContains(created.body(), "\"partnerImpact\":true");
        assertContains(created.body(), "\"businessVolumeDelta\"");
    }

    @Test
    void userCannotOpenForeignClaim() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> foreign = getAuthorized("/api/order/claims/CLM-012-OTHER", userContextId);
        assertEquals(403, foreign.statusCode());
        assertContains(foreign.body(), "STR_MNEMO_ORDER_CLAIM_ACCESS_DENIED");
        assertFalse(foreign.body().contains("attachments"));
    }

    @Test
    void supportCanOpenClaimWithAuditContext() throws Exception {
        String userContextId = loginAs("support");

        HttpResponse<String> details = getAuthorized("/api/order/claims/CLM-012-001?supportCustomerId=customer-012&reason=CLAIM_HELP", userContextId);
        assertEquals(200, details.statusCode());
        assertContains(details.body(), "\"claimId\":\"CLM-012-001\"");
        assertContains(details.body(), "\"auditRecorded\":true");
        assertFalse(details.body().contains("privateComment"));
    }

    public void assertFeatureGreenPath() throws Exception {
        customerCreatesClaimForDeliveredOrder();
        customerSeesClaimHistoryAndDetails();
        claimValidationRejectsUnavailableItem();
        partnerSeesPartnerImpactForSupplementaryClaim();
        userCannotOpenForeignClaim();
        supportCanOpenClaimWithAuditContext();
    }

    private void createDefaultClaim(String userContextId) throws Exception {
        String body = "{\"orderNumber\":\"ORD-011-MAIN\",\"reasonCode\":\"DAMAGED_ITEM\",\"requestedResolution\":\"REFUND\",\"items\":[{\"sku\":\"100-011\",\"quantity\":1}]}";
        postAuthorized("/api/order/claims", body, userContextId, userContextId + "-claim-history");
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

    private HttpResponse<String> postAuthorized(String path, String body, String userContextId, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}