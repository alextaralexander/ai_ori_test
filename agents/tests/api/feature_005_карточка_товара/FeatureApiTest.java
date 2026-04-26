package com.bestorigin.tests.feature005;

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
    void customerCanOpenProductCardWithMediaInformationAndRecommendations() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = get("/api/catalog/products/BOG-CREAM-001?audience=CUSTOMER");

        assertTrue(userContextId.startsWith("customer-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"productCode\":\"BOG-CREAM-001\"");
        assertContains(response.body(), "\"name\"");
        assertContains(response.body(), "\"media\"");
        assertContains(response.body(), "\"information\"");
        assertContains(response.body(), "\"ingredients\"");
        assertContains(response.body(), "\"recommendations\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void unknownProductCardReturnsMnemonicNotFound() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = get("/api/catalog/products/UNKNOWN-PRODUCT?audience=CUSTOMER");

        assertTrue(userContextId.startsWith("customer-api-session"));
        assertEquals(404, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void customerCanAddProductToCartFromProductCard() throws Exception {
        String userContextId = loginAs("customer");
        String body = """
                {"productCode":"BOG-CREAM-001","quantity":2,"audience":"CUSTOMER","userContextId":"%s","source":"PRODUCT_CARD"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/catalog/cart/items", body);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_CART_ITEM_ADDED\"");
        assertContains(response.body(), "\"totalQuantity\":2");
    }

    @Test
    void partnerCartAddFromProductCardKeepsPartnerContext() throws Exception {
        String userContextId = loginAs("partner");
        String body = """
                {"productCode":"BOG-CREAM-001","quantity":1,"audience":"PARTNER","userContextId":"%s","partnerContextId":"partner-context-api-test","source":"PRODUCT_CARD"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/catalog/cart/items", body);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_CART_ITEM_ADDED\"");
        assertContains(response.body(), "\"partnerContext\":true");
    }

    @Test
    void unavailableProductCardCannotBeAddedToCart() throws Exception {
        String userContextId = loginAs("customer");

        String body = """
                {"productCode":"BOG-SOLDOUT-001","quantity":1,"audience":"CUSTOMER","userContextId":"%s","source":"PRODUCT_CARD"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/catalog/cart/items", body);

        assertEquals(409, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_ITEM_UNAVAILABLE\"");
        assertFalse(response.body().contains("Exception"));
    }

    public void assertFeatureGreenPath() throws Exception {
        customerCanOpenProductCardWithMediaInformationAndRecommendations();
        unknownProductCardReturnsMnemonicNotFound();
        customerCanAddProductToCartFromProductCard();
        partnerCartAddFromProductCardKeepsPartnerContext();
        unavailableProductCardCannotBeAddedToCart();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
