package com.bestorigin.tests.feature009;

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
    void customerCanReadEmptyCartAndAddProduct() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> empty = getAuthorized("/api/cart/current", userContextId);
        assertEquals(200, empty.statusCode());
        assertContains(empty.body(), "\"cartType\":\"MAIN\"");
        assertContains(empty.body(), "\"lines\"");

        String body = "{\"productCode\":\"BOG-CREAM-001\",\"quantity\":2,\"source\":\"PRODUCT_CARD\",\"campaignId\":\"CMP-2026-05\"}";
        HttpResponse<String> added = postAuthorized("/api/cart/items", body, userContextId, userContextId + "-cart-add-001");
        assertEquals(200, added.statusCode());
        assertContains(added.body(), "\"productCode\":\"BOG-CREAM-001\"");
        assertContains(added.body(), "\"quantity\":2");
        assertContains(added.body(), "\"messageCode\":\"STR_MNEMO_CART_RECALCULATED\"");
        assertFalse(added.body().contains("Exception"));
    }

    @Test
    void customerCanChangeQuantityAndRemoveLine() throws Exception {
        String userContextId = loginAs("customer");
        String addBody = "{\"productCode\":\"BOG-CREAM-001\",\"quantity\":1,\"source\":\"SEARCH_RESULT\",\"campaignId\":\"CMP-2026-05\"}";
        HttpResponse<String> added = postAuthorized("/api/cart/items", addBody, userContextId, userContextId + "-cart-line-001");
        assertEquals(200, added.statusCode());

        String lineId = extractJsonString(added.body(), "lineId", "00000000-0000-0000-0000-000000009001");
        HttpResponse<String> changed = patchAuthorized("/api/cart/items/" + lineId, "{\"quantity\":3,\"expectedVersion\":0}", userContextId, userContextId + "-cart-change-001");
        assertEquals(200, changed.statusCode());
        assertContains(changed.body(), "\"quantity\":3");

        HttpResponse<String> removed = deleteAuthorized("/api/cart/items/" + lineId, userContextId, userContextId + "-cart-remove-001");
        assertEquals(200, removed.statusCode());
        assertContains(removed.body(), "\"messageCode\"");
    }

    @Test
    void unavailableItemBlocksCheckoutValidation() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"productCode\":\"BOG-REMOVED-003\",\"quantity\":1,\"source\":\"PRODUCT_CARD\",\"campaignId\":\"CMP-2026-05\"}";
        HttpResponse<String> added = postAuthorized("/api/cart/items", body, userContextId, userContextId + "-removed-001");
        assertTrue(added.statusCode() == 200 || added.statusCode() == 400);
        assertContains(added.body(), "STR_MNEMO_CART_ITEM_UNAVAILABLE");

        HttpResponse<String> validation = postAuthorized("/api/cart/validate", "{}", userContextId, userContextId + "-validate-001");
        assertEquals(200, validation.statusCode());
        assertContains(validation.body(), "\"valid\":false");
        assertContains(validation.body(), "\"messageCode\":\"STR_MNEMO_CART_ITEM_UNAVAILABLE\"");
    }

    @Test
    void customerCanReadAndApplyShoppingOffer() throws Exception {
        String userContextId = loginAs("customer");
        postAuthorized("/api/cart/items", "{\"productCode\":\"BOG-CREAM-001\",\"quantity\":2,\"source\":\"PRODUCT_CARD\",\"campaignId\":\"CMP-2026-05\"}", userContextId, userContextId + "-offer-seed-001");

        HttpResponse<String> offers = getAuthorized("/api/cart/shopping-offers", userContextId);
        assertEquals(200, offers.statusCode());
        assertContains(offers.body(), "\"offers\"");
        assertContains(offers.body(), "\"offerId\":\"SET-GLOW-001\"");

        HttpResponse<String> applied = postAuthorized("/api/cart/shopping-offers/SET-GLOW-001/apply", "{}", userContextId, userContextId + "-offer-apply-001");
        assertEquals(200, applied.statusCode());
        assertContains(applied.body(), "\"offerId\":\"SET-GLOW-001\"");
        assertContains(applied.body(), "\"messageCode\":\"STR_MNEMO_CART_RECALCULATED\"");
    }

    @Test
    void partnerCanManageSupplementaryOrderSeparately() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> supplementary = getAuthorized("/api/cart/supplementary/current", userContextId);
        assertEquals(200, supplementary.statusCode());
        assertContains(supplementary.body(), "\"cartType\":\"SUPPLEMENTARY\"");

        String body = "{\"productCode\":\"BOG-CREAM-001\",\"quantity\":4,\"source\":\"SUPPLEMENTARY_OFFER\",\"campaignId\":\"CMP-2026-05\"}";
        HttpResponse<String> added = postAuthorized("/api/cart/supplementary/items", body, userContextId, userContextId + "-supp-add-001");
        assertEquals(200, added.statusCode());
        assertContains(added.body(), "\"cartType\":\"SUPPLEMENTARY\"");
        assertContains(added.body(), "\"productCode\":\"BOG-CREAM-001\"");

        HttpResponse<String> offers = getAuthorized("/api/cart/supplementary/shopping-offers", userContextId);
        assertEquals(200, offers.statusCode());
        assertContains(offers.body(), "\"cartType\":\"SUPPLEMENTARY\"");
        assertFalse(offers.body().contains("\"cartType\":\"MAIN\""));
    }

    @Test
    void supportCanReadPermittedCartView() throws Exception {
        String userContextId = loginAs("order-support");

        HttpResponse<String> response = getAuthorized("/api/cart/support/users/customer-009/current?cartType=MAIN", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"cartType\":\"MAIN\"");
        assertFalse(response.body().contains("\"paymentToken\""));
        assertFalse(response.body().contains("Exception"));
    }

    public void assertFeatureGreenPath() throws Exception {
        customerCanReadEmptyCartAndAddProduct();
        customerCanChangeQuantityAndRemoveLine();
        unavailableItemBlocksCheckoutValidation();
        customerCanReadAndApplyShoppingOffer();
        partnerCanManageSupplementaryOrderSeparately();
        supportCanReadPermittedCartView();
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

    private HttpResponse<String> patchAuthorized(String path, String body, String userContextId, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Idempotency-Key", idempotencyKey)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> deleteAuthorized(String path, String userContextId, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Idempotency-Key", idempotencyKey)
                .DELETE()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static String extractJsonString(String body, String fieldName, String fallback) {
        String marker = "\"" + fieldName + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback;
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
