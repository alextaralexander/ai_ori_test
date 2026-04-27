package com.bestorigin.tests.feature011;

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
    void customerCanSearchOwnOrderHistoryAndOpenDetails() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> history = getAuthorized("/api/order/order-history?query=ORD-011-MAIN&campaignId=CMP-2026-05", userContextId);
        assertEquals(200, history.statusCode());
        assertContains(history.body(), "\"orderNumber\":\"ORD-011-MAIN\"");
        assertContains(history.body(), "\"paymentStatus\":\"PAID\"");
        assertContains(history.body(), "\"deliveryStatus\":\"IN_TRANSIT\"");
        assertFalse(history.body().contains("ORD-011-OTHER"));

        HttpResponse<String> details = getAuthorized("/api/order/order-history/ORD-011-MAIN", userContextId);
        assertEquals(200, details.statusCode());
        assertContains(details.body(), "\"items\"");
        assertContains(details.body(), "\"events\"");
        assertContains(details.body(), "STR_MNEMO_ORDER_CREATED");
        assertContains(details.body(), "\"repeatOrderAvailable\":true");
        assertFalse(details.body().contains("NullPointerException"));
    }

    @Test
    void customerCanContinuePendingPaymentFromDetails() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> details = getAuthorized("/api/order/order-history/ORD-011-PAY", userContextId);
        assertEquals(200, details.statusCode());
        assertContains(details.body(), "\"paymentStatus\":\"PENDING\"");
        assertContains(details.body(), "\"paymentActionAvailable\":true");
        assertContains(details.body(), "STR_MNEMO_ORDER_PAYMENT_PENDING");
    }

    @Test
    void partnerSeesSupplementaryOrderSeparately() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> history = getAuthorized("/api/order/order-history?orderType=SUPPLEMENTARY", userContextId);
        assertEquals(200, history.statusCode());
        assertContains(history.body(), "\"orderNumber\":\"ORD-011-SUPP\"");
        assertContains(history.body(), "\"orderType\":\"SUPPLEMENTARY\"");

        HttpResponse<String> details = getAuthorized("/api/order/order-history/ORD-011-SUPP", userContextId);
        assertEquals(200, details.statusCode());
        assertContains(details.body(), "\"orderType\":\"SUPPLEMENTARY\"");
        assertContains(details.body(), "\"repeatOrderAvailable\":true");
        assertContains(details.body(), "\"businessVolume\"");
    }

    @Test
    void repeatOrderAddsOnlyAvailableLines() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> repeated = postAuthorized("/api/order/order-history/ORD-011-MAIN/repeat", "{}", userContextId, userContextId + "-repeat-001");
        assertEquals(200, repeated.statusCode());
        assertContains(repeated.body(), "\"status\":\"PARTIAL\"");
        assertContains(repeated.body(), "\"addedItems\"");
        assertContains(repeated.body(), "\"rejectedItems\"");
        assertContains(repeated.body(), "STR_MNEMO_ORDER_REPEAT_PARTIAL");
    }

    @Test
    void userCannotOpenForeignOrder() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> foreign = getAuthorized("/api/order/order-history/ORD-011-OTHER", userContextId);
        assertEquals(403, foreign.statusCode());
        assertContains(foreign.body(), "STR_MNEMO_ORDER_HISTORY_ACCESS_DENIED");
        assertFalse(foreign.body().contains("addressLine"));
        assertFalse(foreign.body().contains("paymentSessionId"));
    }

    @Test
    void supportCanOpenOrderDetailsWithAuditContext() throws Exception {
        String userContextId = loginAs("support");

        HttpResponse<String> details = getAuthorized("/api/order/order-history/ORD-011-MAIN?supportCustomerId=customer-011&reason=ORDER_HELP", userContextId);
        assertEquals(200, details.statusCode());
        assertContains(details.body(), "\"orderNumber\":\"ORD-011-MAIN\"");
        assertContains(details.body(), "\"auditRecorded\":true");
        assertFalse(details.body().contains("cardPan"));
    }

    public void assertFeatureGreenPath() throws Exception {
        customerCanSearchOwnOrderHistoryAndOpenDetails();
        customerCanContinuePendingPaymentFromDetails();
        partnerSeesSupplementaryOrderSeparately();
        repeatOrderAddsOnlyAvailableLines();
        userCannotOpenForeignOrder();
        supportCanOpenOrderDetailsWithAuditContext();
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
