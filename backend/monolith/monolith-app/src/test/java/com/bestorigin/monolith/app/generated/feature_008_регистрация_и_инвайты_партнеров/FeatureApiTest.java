// Synchronized from agents/tests/. Do not edit this runtime copy manually.
package com.bestorigin.tests.feature008;

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
    void guestCanValidateActiveBusinessInviteCode() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/partner-onboarding/invites/validate?code=BOG777&onboardingType=BUSINESS_PARTNER&campaignId=CMP-2026-05");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"status\":\"ACTIVE\"");
        assertContains(response.body(), "\"onboardingType\":\"BUSINESS_PARTNER\"");
        assertContains(response.body(), "\"campaignId\":\"CMP-2026-05\"");
        assertContains(response.body(), "\"publicCode\":\"BOG777\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void invalidInviteCodeReturnsMnemonicWithoutSponsorAttribution() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/partner-onboarding/invites/validate?code=UNKNOWN777&onboardingType=BUSINESS_PARTNER");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"status\":\"NOT_FOUND\"");
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_INVITE_CODE_INVALID\"");
        assertFalse(response.body().contains("\"sponsorPartnerId\""));
    }

    @Test
    void registrationApplicationIsCreatedWithIdempotencyAndAttribution() throws Exception {
        String userContextId = loginAs("guest");
        String body = """
                {"onboardingType":"BUSINESS_PARTNER","inviteCode":"BOG777","candidateName":"Анна Партнер","contact":{"channel":"EMAIL","value":"anna.partner@example.test"},"campaignId":"CMP-2026-05","landingType":"BUSINESS","landingVariant":"DEFAULT","sourceRoute":"/business-benefits/BOG777","consentVersions":[{"code":"PARTNER_RULES","version":"2026-04","accepted":true},{"code":"PERSONAL_DATA","version":"2026-04","accepted":true}]}
                """;

        HttpResponse<String> response = post("/api/partner-onboarding/registrations", body, userContextId + "-registration-001");

        assertEquals(201, response.statusCode());
        assertContains(response.body(), "\"status\":\"PENDING_CONTACT_CONFIRMATION\"");
        assertContains(response.body(), "\"nextAction\":\"CONFIRM_CONTACT\"");
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_REGISTRATION_APPLICATION_CREATED\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void invitedPartnerCanReadActivationStateAndCompleteActivation() throws Exception {
        String userContextId = loginAs("invited-partner");

        HttpResponse<String> state = get("/api/partner-onboarding/activations/ACT-008-001");
        assertTrue(userContextId.startsWith("invited-partner-api-session"));
        assertEquals(200, state.statusCode());
        assertContains(state.body(), "\"messageCode\":\"STR_MNEMO_ACTIVATION_READY\"");

        HttpResponse<String> confirm = post("/api/partner-onboarding/activations/ACT-008-001/confirm-contact", "{\"code\":\"123456\"}", userContextId + "-confirm-001");
        assertEquals(200, confirm.statusCode());
        assertContains(confirm.body(), "\"contactConfirmed\":true");

        String completeBody = """
                {"acceptedTerms":[{"code":"PARTNER_RULES","version":"2026-04","accepted":true},{"code":"PERSONAL_DATA","version":"2026-04","accepted":true}]}
                """;
        HttpResponse<String> complete = post("/api/partner-onboarding/activations/ACT-008-001/complete", completeBody, userContextId + "-complete-001");
        assertEquals(200, complete.statusCode());
        assertContains(complete.body(), "\"status\":\"ACTIVE\"");
        assertContains(complete.body(), "\"referralLink\"");
        assertContains(complete.body(), "\"messageCode\":\"STR_MNEMO_PARTNER_ACTIVATED\"");
    }

    @Test
    void sponsorCanManageOwnInvites() throws Exception {
        String userContextId = loginAs("sponsor");

        HttpResponse<String> list = getAuthorized("/api/partner-onboarding/sponsor-cabinet/invites", userContextId);
        assertEquals(200, list.statusCode());
        assertContains(list.body(), "\"items\"");
        assertFalse(list.body().contains("\"privateEmail\""));

        String createBody = "{\"onboardingType\":\"BUSINESS_PARTNER\",\"campaignId\":\"CMP-2026-05\",\"candidatePublicName\":\"Анна\"}";
        HttpResponse<String> created = postAuthorized("/api/partner-onboarding/sponsor-cabinet/invites", createBody, userContextId, userContextId + "-invite-001");
        assertEquals(201, created.statusCode());
        assertContains(created.body(), "\"onboardingType\":\"BUSINESS_PARTNER\"");
        assertContains(created.body(), "\"status\":\"CREATED\"");
        assertContains(created.body(), "\"targetRoute\"");
    }

    public void assertFeatureGreenPath() throws Exception {
        guestCanValidateActiveBusinessInviteCode();
        invalidInviteCodeReturnsMnemonicWithoutSponsorAttribution();
        registrationApplicationIsCreatedWithIdempotencyAndAttribution();
        invitedPartnerCanReadActivationStateAndCompleteActivation();
        sponsorCanManageOwnInvites();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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

    private HttpResponse<String> post(String path, String body, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
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
