package com.bestorigin.tests.feature002;

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
    void guestCanLoadNewsFeedAndOpenContentPage() throws Exception {
        String guestToken = loginAs("guest");

        HttpResponse<String> news = get("/api/public-content/news?audience=GUEST", guestToken);

        assertEquals(200, news.statusCode());
        assertContains(news.body(), "\"newsKey\":\"spring-collection\"");
        assertContains(news.body(), "\"contentId\":\"brand-care-guide\"");
        assertContains(news.body(), "\"targetRoute\":\"/content/brand-care-guide\"");

        HttpResponse<String> content = get("/api/public-content/content/brand-care-guide?audience=GUEST", guestToken);

        assertEquals(200, content.statusCode());
        assertContains(content.body(), "\"contentId\":\"brand-care-guide\"");
        assertContains(content.body(), "\"templateCode\":\"GUIDE\"");
        assertContains(content.body(), "\"sectionType\":\"RICH_TEXT\"");
        assertContains(content.body(), "\"fileType\":\"PDF\"");
        assertContains(content.body(), "\"targetRoute\":\"/catalog\"");
    }

    @Test
    void guestCanLoadPublishedOfferWithCatalogCta() throws Exception {
        String guestToken = loginAs("guest");

        HttpResponse<String> offer = get("/api/public-content/offers/spring-offer?audience=GUEST", guestToken);

        assertEquals(200, offer.statusCode());
        assertContains(offer.body(), "\"offerId\":\"spring-offer\"");
        assertContains(offer.body(), "\"sectionType\":\"CONDITIONS\"");
        assertContains(offer.body(), "\"targetValue\":\"/catalog\"");
        assertContains(offer.body(), "\"productRef\":\"spring-campaign\"");
    }

    @Test
    void customerReceivesPersonalContentCtaWithoutLosingPublicContent() throws Exception {
        String customerToken = loginAs("customer");

        HttpResponse<String> content = get("/api/public-content/content/brand-care-guide?audience=CUSTOMER", customerToken);

        assertEquals(200, content.statusCode());
        assertContains(content.body(), "\"contentId\":\"brand-care-guide\"");
        assertContains(content.body(), "\"audience\":\"CUSTOMER\"");
        assertContains(content.body(), "\"targetValue\":\"/cart\"");
        assertContains(content.body(), "\"targetValue\":\"/catalog\"");
    }

    @Test
    void unpublishedContentDoesNotRevealBody() throws Exception {
        String guestToken = loginAs("guest");

        HttpResponse<String> response = get("/api/public-content/content/expired-material?audience=GUEST", guestToken);

        assertEquals(404, response.statusCode());
        assertContains(response.body(), "\"code\":\"STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND\"");
        assertFalse(response.body().contains("expired-material-body"));
    }

    public void assertFeatureGreenPath() throws Exception {
        guestCanLoadNewsFeedAndOpenContentPage();
        guestCanLoadPublishedOfferWithCatalogCta();
        customerReceivesPersonalContentCtaWithoutLosingPublicContent();
        unpublishedContentDoesNotRevealBody();
    }

    private HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .GET();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private String loginAs(String role) throws IOException, InterruptedException {
        if ("guest".equals(role)) {
            return "";
        }
        String body = "{\"username\":\"" + role + "\",\"password\":\"password\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/api/auth/test-login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());
        String marker = "\"token\":\"";
        int start = response.body().indexOf(marker);
        assertTrue(start >= 0, "Login response must contain token");
        int tokenStart = start + marker.length();
        int tokenEnd = response.body().indexOf('"', tokenStart);
        assertTrue(tokenEnd > tokenStart, "Login token must be non-empty");
        return response.body().substring(tokenStart, tokenEnd);
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
