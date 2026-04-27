// Synchronized from agents/tests/. Do not edit this generated runtime copy manually.
package com.bestorigin.tests.feature013;

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
    void customerSeesProfileOverviewAndReadiness() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> overview = getAuthorized("/api/profile", userContextId);
        assertEquals(200, overview.statusCode());
        assertContains(overview.body(), "\"sections\"");
        assertContains(overview.body(), "\"readiness\"");
        assertContains(overview.body(), "\"flow\":\"CHECKOUT\"");
        assertFalse(overview.body().contains("documentPayload"));
    }

    @Test
    void customerUpdatesGeneralProfileData() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"firstName\":\"Анна\",\"lastName\":\"Иванова\",\"birthDate\":\"1990-04-15\",\"gender\":\"FEMALE\",\"preferredLanguage\":\"ru\"}";

        HttpResponse<String> updated = putAuthorized("/api/profile/general", body, userContextId);
        assertEquals(200, updated.statusCode());
        assertContains(updated.body(), "\"lastName\":\"Иванова\"");
        assertContains(updated.body(), "\"preferredLanguage\":\"ru\"");

        HttpResponse<String> audit = getAuthorized("/api/profile/audit-events", userContextId);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "\"sectionKey\":\"GENERAL\"");
    }

    @Test
    void customerAddsContactAndStartsVerification() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"contactType\":\"EMAIL\",\"value\":\"customer013@example.test\",\"primary\":true}";

        HttpResponse<String> contacts = postAuthorized("/api/profile/contacts", body, userContextId, userContextId + "-contact-001");
        assertEquals(200, contacts.statusCode());
        assertContains(contacts.body(), "\"contactType\":\"EMAIL\"");
        assertContains(contacts.body(), "\"maskedValue\"");
        assertFalse(contacts.body().contains("customer013@example.test"));

        HttpResponse<String> verification = postAuthorized("/api/profile/contacts/00000000-0000-0000-0000-000000000013/verification", "{}", userContextId, userContextId + "-verify-001");
        assertEquals(200, verification.statusCode());
        assertContains(verification.body(), "\"verificationStatus\":\"REQUIRES_VERIFICATION\"");
    }

    @Test
    void customerAddsDefaultAddressAndReadinessBecomesReady() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"countryCode\":\"RU\",\"city\":\"Москва\",\"street\":\"Тверская\",\"house\":\"1\",\"postalCode\":\"101000\",\"default\":true}";

        HttpResponse<String> addresses = postAuthorized("/api/profile/addresses", body, userContextId, userContextId + "-address-001");
        assertEquals(200, addresses.statusCode());
        assertContains(addresses.body(), "\"default\":true");

        HttpResponse<String> readiness = getAuthorized("/api/profile/readiness?flow=CHECKOUT", userContextId);
        assertEquals(200, readiness.statusCode());
        assertContains(readiness.body(), "\"flow\":\"CHECKOUT\"");
        assertContains(readiness.body(), "\"ready\":true");
    }

    @Test
    void lockedAddressCannotBeDeleted() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> locked = deleteAuthorized("/api/profile/addresses/00000000-0000-0000-0000-000000000013", userContextId);
        assertEquals(409, locked.statusCode());
        assertContains(locked.body(), "STR_MNEMO_PROFILE_ADDRESS_LOCKED");
        assertContains(locked.body(), "ADDRESS_LOCKED_BY_ACTIVE_ORDER");
    }

    @Test
    void partnerAddsDocumentWithMaskedResponse() throws Exception {
        String userContextId = loginAs("partner");
        String body = "{\"documentType\":\"PASSPORT\",\"documentPayload\":{\"number\":\"4510123456\",\"issuedBy\":\"MVD\"}}";

        HttpResponse<String> documents = postAuthorized("/api/profile/documents", body, userContextId, userContextId + "-document-001");
        assertEquals(200, documents.statusCode());
        assertContains(documents.body(), "\"documentType\":\"PASSPORT\"");
        assertContains(documents.body(), "\"documentNumberMasked\"");
        assertFalse(documents.body().contains("4510123456"));
    }

    @Test
    void weakPasswordReturnsMnemonic() throws Exception {
        String userContextId = loginAs("customer");
        String body = "{\"currentPassword\":\"current-password\",\"newPassword\":\"123\"}";

        HttpResponse<String> rejected = postAuthorized("/api/profile/security/password", body, userContextId, userContextId + "-password-weak");
        assertEquals(400, rejected.statusCode());
        assertContains(rejected.body(), "STR_MNEMO_PROFILE_PASSWORD_WEAK");
    }

    @Test
    void userCannotOpenForeignProfile() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> foreign = getAuthorized("/api/profile/support/USR-013-OTHER?reason=PROFILE_HELP", userContextId);
        assertEquals(403, foreign.statusCode());
        assertContains(foreign.body(), "STR_MNEMO_PROFILE_ACCESS_DENIED");
        assertFalse(foreign.body().contains("documentPayload"));
    }

    @Test
    void supportCanOpenProfileWithAuditContext() throws Exception {
        String userContextId = loginAs("support");

        HttpResponse<String> supportView = getAuthorized("/api/profile/support/customer-013?reason=PROFILE_HELP", userContextId);
        assertEquals(200, supportView.statusCode());
        assertContains(supportView.body(), "\"auditRecorded\":true");
        assertContains(supportView.body(), "\"sections\"");
        assertFalse(supportView.body().contains("currentPassword"));
    }

    public void assertFeatureGreenPath() throws Exception {
        customerSeesProfileOverviewAndReadiness();
        customerUpdatesGeneralProfileData();
        customerAddsContactAndStartsVerification();
        customerAddsDefaultAddressAndReadinessBecomesReady();
        lockedAddressCannotBeDeleted();
        partnerAddsDocumentWithMaskedResponse();
        weakPasswordReturnsMnemonic();
        userCannotOpenForeignProfile();
        supportCanOpenProfileWithAuditContext();
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

    private HttpResponse<String> deleteAuthorized(String path, String userContextId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .DELETE()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> putAuthorized(String path, String body, String userContextId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
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
