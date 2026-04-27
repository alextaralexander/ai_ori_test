package com.bestorigin.tests.feature016;

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
    void partnerLeaderSeesMlmDashboard() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/mlm-structure/dashboard?campaignId=CAT-2026-05", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"leaderPersonNumber\":\"BOG-016-001\"");
        assertContains(response.body(), "\"groupVolume\"");
        assertContains(response.body(), "STR_MNEMO_MLM_STRUCTURE_DASHBOARD_READY");
    }

    @Test
    void partnerLeaderFiltersCommunityByLevel() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/mlm-structure/community?campaignId=CAT-2026-05&level=2", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-016-002");
        assertContains(response.body(), "\"structureLevel\":2");
        assertContains(response.body(), "\"branchId\":\"BRANCH-SKINCARE\"");
    }

    @Test
    void businessManagerSeesConversionAndTeamActivity() throws Exception {
        String userContextId = loginAs("business-manager");

        HttpResponse<String> conversion = getAuthorized("/api/mlm-structure/conversion?campaignId=CAT-2026-05", userContextId);
        assertEquals(200, conversion.statusCode());
        assertContains(conversion.body(), "\"firstOrderCount\"");
        assertContains(conversion.body(), "\"conversionRatePercent\"");

        HttpResponse<String> activity = getAuthorized("/api/mlm-structure/team-activity?campaignId=CAT-2026-05&riskOnly=true", userContextId);
        assertEquals(200, activity.statusCode());
        assertContains(activity.body(), "RISK_SIGNAL");
        assertContains(activity.body(), "BOG-016-003");
    }

    @Test
    void partnerLeaderSeesUpgradeRequirements() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/mlm-structure/upgrade?campaignId=CAT-2026-05", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"currentRank\":\"SILVER\"");
        assertContains(response.body(), "\"nextRank\":\"GOLD\"");
        assertContains(response.body(), "STR_MNEMO_MLM_STRUCTURE_UPGRADE_READY");
    }

    @Test
    void partnerLeaderOpensPartnerCard() throws Exception {
        String userContextId = loginAs("partner-leader");

        HttpResponse<String> response = getAuthorized("/api/mlm-structure/partners/BOG-016-002?campaignId=CAT-2026-05", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"personNumber\":\"BOG-016-002\"");
        assertContains(response.body(), "\"qualificationProgress\"");
        assertContains(response.body(), "\"linkedActions\"");
        assertContains(response.body(), "STR_MNEMO_MLM_STRUCTURE_PARTNER_CARD_READY");
    }

    @Test
    void customerCannotOpenMlmStructure() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/mlm-structure/dashboard?campaignId=CAT-2026-05", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        partnerLeaderSeesMlmDashboard();
        partnerLeaderFiltersCommunityByLevel();
        businessManagerSeesConversionAndTeamActivity();
        partnerLeaderSeesUpgradeRequirements();
        partnerLeaderOpensPartnerCard();
        customerCannotOpenMlmStructure();
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

    private static String loginAs(String role) {
        return role + "-api-session-" + System.nanoTime();
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}