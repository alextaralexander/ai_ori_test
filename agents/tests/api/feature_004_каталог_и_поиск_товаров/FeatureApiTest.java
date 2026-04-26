package com.bestorigin.tests.feature004;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class FeatureApiTest {

    private static final String BASE_URL = System.getProperty("bestorigin.baseUrl", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void guestCanSearchCatalogWithFiltersAndSorting() throws Exception {
        HttpResponse<String> response = get("/api/catalog/search?audience=GUEST&q=" + encode("cream")
                + "&category=face-care&availability=inStock&promo=true&sort=popular&page=0&size=2");

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"sku\":\"BOG-CREAM-001\"");
        assertContains(response.body(), "\"categorySlug\":\"face-care\"");
        assertContains(response.body(), "\"availability\":\"IN_STOCK\"");
        assertContains(response.body(), "\"promoBadges\":[\"new\",\"campaign-hit\"]");
        assertContains(response.body(), "\"hasNextPage\"");
    }

    @Test
    void emptySearchReturnsMessageCodeAndRecommendations() throws Exception {
        HttpResponse<String> response = get("/api/catalog/search?audience=GUEST&q=" + encode("missing product"));

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"items\":[]");
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_SEARCH_EMPTY\"");
        assertContains(response.body(), "\"recommendations\"");
        assertContains(response.body(), "\"sku\":\"BOG-CREAM-001\"");
    }

    @Test
    void customerCanAddAvailableProductToCart() throws Exception {
        String userContextId = "customer-api-test-" + System.nanoTime();
        String body = """
                {"productId":"11111111-1111-1111-1111-111111111111","quantity":1,"audience":"CUSTOMER","userContextId":"%s","searchUrl":"/search?q=cream"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/catalog/cart/items", body);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_CART_ITEM_ADDED\"");
        assertContains(response.body(), "\"itemsCount\":");
        assertContains(response.body(), "\"totalQuantity\":");
    }

    @Test
    void partnerSearchKeepsPartnerContextOnCartAdd() throws Exception {
        HttpResponse<String> search = get("/api/catalog/search?audience=PARTNER&promo=true");

        assertEquals(200, search.statusCode());
        assertContains(search.body(), "\"partner\"");
        assertContains(search.body(), "\"canAddToCart\":true");

        String userContextId = "partner-api-test-" + System.nanoTime();
        String body = """
                {"productId":"11111111-1111-1111-1111-111111111111","quantity":2,"audience":"PARTNER","userContextId":"%s","searchUrl":"/search?promo=true"}
                """.formatted(userContextId);

        HttpResponse<String> cart = post("/api/catalog/cart/items", body);

        assertEquals(200, cart.statusCode());
        assertContains(cart.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_CART_ITEM_ADDED\"");
        assertContains(cart.body(), "\"totalQuantity\":2");
    }

    @Test
    void unavailableProductCannotBeAddedToCart() throws Exception {
        String body = """
                {"productId":"33333333-3333-3333-3333-333333333333","quantity":1,"audience":"CUSTOMER","userContextId":"customer-api-test","searchUrl":"/search?availability=outOfStock"}
                """;

        HttpResponse<String> response = post("/api/catalog/cart/items", body);

        assertEquals(409, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_CATALOG_ITEM_UNAVAILABLE\"");
        assertFalse(response.body().contains("Exception"));
    }

    public void assertFeatureGreenPath() throws Exception {
        guestCanSearchCatalogWithFiltersAndSorting();
        emptySearchReturnsMessageCodeAndRecommendations();
        customerCanAddAvailableProductToCart();
        partnerSearchKeepsPartnerContextOnCartAdd();
        unavailableProductCannotBeAddedToCart();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
