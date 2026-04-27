// GENERATED FROM agents/tests/api; DO NOT EDIT MANUALLY.
package com.bestorigin.tests.feature030;

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
    void catalogManagerCreatesCampaignIssuePdfPageAndHotspot() throws Exception {
        String token = extractJsonString(loginAs("catalog-manager").body(), "token", "test-token-catalog-manager");
        String campaignBody = "{\"campaignCode\":\"CAM-2026-05\",\"name\":\"Майский каталог Best Ori Gin\",\"locale\":\"ru\",\"audience\":\"CUSTOMER_AND_PARTNER\",\"startsAt\":\"2026-05-01T00:00:00Z\",\"endsAt\":\"2026-05-21T23:59:59Z\"}";

        HttpResponse<String> campaign = sendAuthorized("/api/admin-catalog/campaigns", token, "POST", campaignBody, "CATALOG-030-CAMPAIGN");
        assertEquals(201, campaign.statusCode());
        assertContains(campaign.body(), "CAM-2026-05");
        assertContains(campaign.body(), "STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_SAVED");
        assertNoHardcodedRussianUiText(campaign.body());

        String campaignId = extractJsonString(campaign.body(), "campaignId", "00000000-0000-0000-0000-000000000030");
        String issueBody = "{\"issueCode\":\"ISSUE-2026-05\",\"publicationAt\":\"2026-05-01T00:00:00Z\",\"archiveAt\":\"2026-05-22T00:00:00Z\",\"freezeStartsAt\":\"2026-05-20T18:00:00Z\",\"rolloverWindowStartsAt\":\"2026-05-21T21:00:00Z\",\"rolloverWindowEndsAt\":\"2026-05-21T23:59:59Z\"}";
        HttpResponse<String> issue = sendAuthorized("/api/admin-catalog/campaigns/" + campaignId + "/issues", token, "POST", issueBody, "CATALOG-030-ISSUE");
        assertEquals(201, issue.statusCode());
        assertContains(issue.body(), "ISSUE-2026-05");
        assertContains(issue.body(), "STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED");

        String issueId = extractJsonString(issue.body(), "issueId", "00000000-0000-0000-0000-000000000130");
        String materialBody = "{\"materialType\":\"PDF\",\"fileName\":\"best-origin-may-2026.pdf\",\"mimeType\":\"application/pdf\",\"sizeBytes\":4096,\"checksum\":\"sha256:catalog-may-2026\",\"storageKey\":\"s3://catalogs/may-2026.pdf\"}";
        HttpResponse<String> material = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/materials", token, "POST", materialBody, "CATALOG-030-PDF");
        assertEquals(201, material.statusCode());
        assertContains(material.body(), "STR_MNEMO_ADMIN_CATALOG_MATERIAL_SAVED");

        String materialId = extractJsonString(material.body(), "materialId", "00000000-0000-0000-0000-000000000230");
        HttpResponse<String> approved = sendAuthorized("/api/admin-catalog/materials/" + materialId + "/approve", token, "POST", "{}", "CATALOG-030-PDF");
        assertEquals(200, approved.statusCode());
        assertContains(approved.body(), "APPROVED");
        assertContains(approved.body(), "STR_MNEMO_ADMIN_CATALOG_PDF_APPROVED");

        String pageBody = "{\"pageNumber\":1,\"imageUrl\":\"https://cdn.bestorigin.test/catalogs/may-2026/page-1.jpg\",\"widthPx\":1440,\"heightPx\":2048}";
        HttpResponse<String> page = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/pages", token, "POST", pageBody, "CATALOG-030-PAGE");
        assertEquals(201, page.statusCode());
        assertContains(page.body(), "STR_MNEMO_ADMIN_CATALOG_PAGE_SAVED");

        String hotspotBody = "{\"pageNumber\":1,\"sku\":\"BOG-SERUM-001\",\"promoCode\":\"SPRING-HITS\",\"xRatio\":0.20,\"yRatio\":0.35,\"widthRatio\":0.18,\"heightRatio\":0.08}";
        HttpResponse<String> hotspot = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/hotspots", token, "POST", hotspotBody, "CATALOG-030-HOTSPOT");
        assertEquals(201, hotspot.statusCode());
        assertContains(hotspot.body(), "STR_MNEMO_ADMIN_CATALOG_HOTSPOT_SAVED");
    }

    @Test
    void duplicateCampaignAndInvalidHotspotReturnMnemonicCodes() throws Exception {
        String token = extractJsonString(loginAs("catalog-manager").body(), "token", "test-token-catalog-manager");
        String campaignBody = "{\"campaignCode\":\"CAM-2026-05\",\"name\":\"Майский каталог Best Ori Gin\",\"locale\":\"ru\",\"audience\":\"CUSTOMER_AND_PARTNER\",\"startsAt\":\"2026-05-01T00:00:00Z\",\"endsAt\":\"2026-05-21T23:59:59Z\"}";

        sendAuthorized("/api/admin-catalog/campaigns", token, "POST", campaignBody, "CATALOG-030-DUP-1");
        HttpResponse<String> duplicate = sendAuthorized("/api/admin-catalog/campaigns", token, "POST", campaignBody, "CATALOG-030-DUP-2");
        assertEquals(409, duplicate.statusCode());
        assertContains(duplicate.body(), "STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_CODE_CONFLICT");

        HttpResponse<String> invalid = sendAuthorized("/api/admin-catalog/issues/00000000-0000-0000-0000-000000000130/hotspots", token, "POST", "{\"pageNumber\":1,\"sku\":\"BOG-SERUM-001\",\"xRatio\":1.20,\"yRatio\":0.35,\"widthRatio\":0.18,\"heightRatio\":0.08}", "CATALOG-030-HOTSPOT-INVALID");
        assertEquals(400, invalid.statusCode());
        assertContains(invalid.body(), "STR_MNEMO_ADMIN_CATALOG_HOTSPOT_INVALID");
        assertNoHardcodedRussianUiText(invalid.body());
    }

    @Test
    void freezeWindowBlocksMaterialChangeAndRolloverIsIdempotent() throws Exception {
        String token = extractJsonString(loginAs("catalog-manager").body(), "token", "test-token-catalog-manager");
        String issueId = "00000000-0000-0000-0000-000000000130";

        HttpResponse<String> frozen = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/materials", token, "POST", "{\"materialType\":\"PDF\",\"fileName\":\"frozen.pdf\",\"mimeType\":\"application/pdf\",\"sizeBytes\":1024,\"checksum\":\"sha256:frozen\",\"storageKey\":\"s3://catalogs/frozen.pdf\",\"freezeOverride\":false}", "CATALOG-030-FREEZE");
        assertEquals(409, frozen.statusCode());
        assertContains(frozen.body(), "STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE");

        HttpResponse<String> validation = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/validate-links", token, "POST", "{}", "CATALOG-030-LINKS");
        assertEquals(200, validation.statusCode());
        assertContains(validation.body(), "validHotspots");
        assertContains(validation.body(), "STR_MNEMO_ADMIN_CATALOG_LINKS_VALID");

        HttpResponse<String> first = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/rollover", token, "POST", "{}", "rollover-may-2026");
        HttpResponse<String> second = sendAuthorized("/api/admin-catalog/issues/" + issueId + "/rollover", token, "POST", "{}", "rollover-may-2026");
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertEquals(extractJsonString(first.body(), "rolloverJobId", "missing"), extractJsonString(second.body(), "rolloverJobId", "different"));
        assertContains(first.body(), "STR_MNEMO_ADMIN_CATALOG_ROLLOVER_COMPLETED");
    }

    @Test
    void archiveAuditAndForbiddenAccessAreExposedSafely() throws Exception {
        String token = extractJsonString(loginAs("catalog-manager").body(), "token", "test-token-catalog-manager");

        HttpResponse<String> archive = getAuthorized("/api/admin-catalog/archive", token);
        assertEquals(200, archive.statusCode());
        assertContains(archive.body(), "ISSUE-2026-04");

        HttpResponse<String> audit = getAuthorized("/api/admin-catalog/audit?actionCode=ROLLOVER_COMPLETED", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("secret"));
        assertFalse(audit.body().contains("privateStoragePath"));

        String forbiddenToken = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");
        HttpResponse<String> forbidden = getAuthorized("/api/admin-catalog/workspace", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_CATALOG_FORBIDDEN");
    }

    public void assertFeatureGreenPath() throws Exception {
        catalogManagerCreatesCampaignIssuePdfPageAndHotspot();
        duplicateCampaignAndInvalidHotspotReturnMnemonicCodes();
        freezeWindowBlocksMaterialChangeAndRolloverIsIdempotent();
        archiveAuditAndForbiddenAccessAreExposedSafely();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException {
        return send(path, method, body, token, idempotencyKey);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = builder.method(method, publisher).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Кампания сохранена"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Каталог опубликован"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Доступ запрещен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
    }

    private static String extractJsonString(String body, String field, String fallback) {
        String marker = "\"" + field + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback;
    }
}
