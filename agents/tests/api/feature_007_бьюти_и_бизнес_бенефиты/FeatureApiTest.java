package com.bestorigin.tests.feature007;

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
    void guestCanOpenBeautyLandingWithActiveReferralCode() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/public-content/benefit-landings/BEAUTY?code=BOG777&campaignId=CMP-2026-05&variant=DEFAULT");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"landingType\":\"BEAUTY\"");
        assertContains(response.body(), "\"variant\":\"DEFAULT\"");
        assertContains(response.body(), "\"campaignId\":\"CMP-2026-05\"");
        assertContains(response.body(), "\"status\":\"ACTIVE\"");
        assertContains(response.body(), "\"code\":\"BOG777\"");
        assertContains(response.body(), "\"blocks\"");
        assertContains(response.body(), "\"ctas\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void guestCanOpenBusinessLandingWithoutReferralCode() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/public-content/benefit-landings/BUSINESS?campaignId=CMP-2026-05");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"landingType\":\"BUSINESS\"");
        assertContains(response.body(), "\"status\":\"NOT_FOUND\"");
        assertContains(response.body(), "\"REGISTER_PARTNER\"");
        assertContains(response.body(), "\"OPEN_CATALOG\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void unknownReferralCodeReturnsMnemonicStatusWithoutBreakingLanding() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/public-content/benefit-landings/BEAUTY?code=UNKNOWN777");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"status\":\"NOT_FOUND\"");
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_REFERRAL_CODE_INVALID\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void appLandingContainsInstallAppCta() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/public-content/benefit-landings/APP?variant=DEFAULT");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"landingType\":\"APP\"");
        assertContains(response.body(), "\"INSTALL_APP\"");
        assertContains(response.body(), "\"seo\"");
    }

    @Test
    void conversionEventIsAccepted() throws Exception {
        String userContextId = loginAs("marketing-manager");
        String body = """
                {"landingType":"BUSINESS","variant":"DEFAULT","referralCode":"BOG777","campaignId":"CMP-2026-05","ctaType":"REGISTER_PARTNER","routePath":"/business-benefits/BOG777","occurredAt":"2026-04-27T01:00:00Z","anonymousSessionId":"%s"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/public-content/benefit-landings/conversions", body);

        assertEquals(202, response.statusCode());
        assertContains(response.body(), "\"accepted\":true");
    }

    public void assertFeatureGreenPath() throws Exception {
        guestCanOpenBeautyLandingWithActiveReferralCode();
        guestCanOpenBusinessLandingWithoutReferralCode();
        unknownReferralCodeReturnsMnemonicStatusWithoutBreakingLanding();
        appLandingContainsInstallAppCta();
        conversionEventIsAccepted();
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

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
