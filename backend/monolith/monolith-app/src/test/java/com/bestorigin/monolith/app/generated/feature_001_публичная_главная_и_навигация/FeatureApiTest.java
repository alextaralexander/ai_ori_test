// Synchronized artifact from agents/tests/api. Do not edit manually.
package com.bestorigin.tests.feature001;

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
    void guestCanLoadPublicHomeNavigationAndCommunityEntryPoint() throws Exception {
        String guestToken = loginAs("guest");

        HttpResponse<String> home = get("/api/public-content/pages/home?audience=GUEST", guestToken);

        assertEquals(200, home.statusCode());
        assertContains(home.body(), "\"pageKey\":\"HOME\"");
        assertContains(home.body(), "\"blockType\":\"HERO\"");
        assertContains(home.body(), "\"targetRoute\":\"/catalog\"");
        assertContains(home.body(), "\"targetRoute\":\"/community\"");
        assertFalse(home.body().contains("STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE"));
    }

    @Test
    void customerCanLoadPersonalEntryPointsWithoutLosingPublicNavigation() throws Exception {
        String customerToken = loginAs("customer");

        HttpResponse<String> home = get("/api/public-content/pages/home?audience=CUSTOMER", customerToken);

        assertEquals(200, home.statusCode());
        assertContains(home.body(), "\"targetRoute\":\"/profile\"");
        assertContains(home.body(), "\"targetRoute\":\"/cart\"");
        assertContains(home.body(), "\"targetRoute\":\"/orders\"");
        assertContains(home.body(), "\"targetRoute\":\"/catalog\"");
        assertContains(home.body(), "\"targetRoute\":\"/community\"");
    }

    @Test
    void partnerCanLoadPartnerOfficeEntryPoint() throws Exception {
        String partnerToken = loginAs("partner");

        HttpResponse<String> home = get("/api/public-content/pages/home?audience=PARTNER", partnerToken);

        assertEquals(200, home.statusCode());
        assertContains(home.body(), "\"targetRoute\":\"/partner-office\"");
        assertContains(home.body(), "\"targetRoute\":\"/benefits\"");
        assertContains(home.body(), "\"targetRoute\":\"/catalog\"");
    }

    @Test
    void communityPageIsPublicAndUsesSharedNavigation() throws Exception {
        String guestToken = loginAs("guest");

        HttpResponse<String> community = get("/api/public-content/pages/community?audience=GUEST", guestToken);

        assertEquals(200, community.statusCode());
        assertContains(community.body(), "\"pageKey\":\"COMMUNITY\"");
        assertContains(community.body(), "\"targetRoute\":\"/community\"");
        assertContains(community.body(), "\"targetRoute\":\"/register\"");
    }

    public void assertFeatureGreenPath() throws Exception {
        guestCanLoadPublicHomeNavigationAndCommunityEntryPoint();
        customerCanLoadPersonalEntryPointsWithoutLosingPublicNavigation();
        partnerCanLoadPartnerOfficeEntryPoint();
        communityPageIsPublicAndUsesSharedNavigation();
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
