// AUTO-GENERATED from agents/tests/. Do not edit manually.
package com.bestorigin.tests.feature017;

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
    void partnerLeaderSeesPartnerOfflineOrderList() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/order/partner-offline-orders?campaignId=CAT-2026-05", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-VIP-017-001");
        assertContains(response.body(), "BOG-016-002");
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFLINE_ORDER_READY");
    }

    @Test
    void partnerLeaderFiltersPartnerOfflineOrders() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/order/partner-offline-orders?campaignId=CAT-2026-05&paymentStatus=PAID&partnerPersonNumber=BOG-016-002", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"paymentStatus\":\"PAID\"");
        assertContains(response.body(), "\"partnerPersonNumber\":\"BOG-016-002\"");
    }

    @Test
    void partnerLeaderOpensPartnerOfflineOrderDetails() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/order/partner-offline-orders/BOG-VIP-017-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"orderNumber\":\"BOG-VIP-017-001\"");
        assertContains(response.body(), "\"businessVolume\"");
        assertContains(response.body(), "\"availableActions\"");
    }

    @Test
    void orderSupportRecordsServiceAdjustment() throws Exception {
        String userContextId = loginAs("order-support");

        HttpResponse<String> response = postAuthorized("/api/order/partner-offline-orders/BOG-VIP-017-001/actions", userContextId, "{\"actionType\":\"SERVICE_ADJUSTMENT\"}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFLINE_ORDER_ADJUSTMENT_RECORDED");
    }

    @Test
    void partnerLeaderCreatesRepeatOrder() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = postAuthorized("/api/order/partner-offline-orders/BOG-VIP-017-001/actions", userContextId, "{\"actionType\":\"REPEAT_ORDER\"}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFLINE_ORDER_REPEAT_CREATED");
    }

    @Test
    void customerCannotOpenPartnerOfflineOrders() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/order/partner-offline-orders?campaignId=CAT-2026-05", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFLINE_ORDER_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        partnerLeaderSeesPartnerOfflineOrderList();
        partnerLeaderFiltersPartnerOfflineOrders();
        partnerLeaderOpensPartnerOfflineOrderDetails();
        orderSupportRecordsServiceAdjustment();
        partnerLeaderCreatesRepeatOrder();
        customerCannotOpenPartnerOfflineOrders();
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

    private HttpResponse<String> postAuthorized(String path, String userContextId, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "feature-017-" + System.nanoTime())
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