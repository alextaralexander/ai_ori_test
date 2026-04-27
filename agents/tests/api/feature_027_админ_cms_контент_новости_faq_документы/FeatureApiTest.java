package com.bestorigin.tests.feature027;

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
    void contentAdminCreatesMaterialAndReceivesMnemonicContract() throws Exception {
        String token = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        String body = "{\"materialType\":\"NEWS\",\"language\":\"ru\",\"slug\":\"spring-campaign-editorial\",\"title\":\"Spring campaign editorial\",\"summary\":\"Campaign content\",\"audience\":\"PUBLIC\",\"blocks\":[{\"blockType\":\"HERO\",\"sortOrder\":1,\"payload\":{\"heading\":\"Spring\"}},{\"blockType\":\"RICH_TEXT\",\"sortOrder\":2,\"payload\":{\"text\":\"Campaign body\"}}],\"seo\":{\"slug\":\"spring-campaign-editorial\",\"title\":\"Spring campaign\",\"description\":\"Best Ori Gin campaign\",\"canonicalUrl\":\"https://bestorigin.com/news/spring-campaign-editorial\",\"robotsPolicy\":\"index,follow\",\"breadcrumbTitle\":\"Spring\"}}";

        HttpResponse<String> response = sendAuthorized("/api/admin-cms/materials", token, "POST", body, "CMS-027-ADMIN");

        assertEquals(201, response.statusCode());
        assertContains(response.body(), "spring-campaign-editorial");
        assertContains(response.body(), "STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED");
        assertFalse(response.body().contains("privateStoragePath"));
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void duplicateSlugReturnsConflictMnemonic() throws Exception {
        String token = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        String body = "{\"materialType\":\"NEWS\",\"language\":\"ru\",\"slug\":\"spring-campaign-editorial\",\"title\":\"Duplicate\",\"audience\":\"PUBLIC\",\"blocks\":[{\"blockType\":\"RICH_TEXT\",\"sortOrder\":1,\"payload\":{\"text\":\"Duplicate\"}}],\"seo\":{\"slug\":\"spring-campaign-editorial\",\"title\":\"Duplicate\",\"description\":\"Duplicate\",\"canonicalUrl\":\"https://bestorigin.com/news/spring-campaign-editorial\",\"robotsPolicy\":\"index,follow\",\"breadcrumbTitle\":\"Duplicate\"}}";

        HttpResponse<String> response = sendAuthorized("/api/admin-cms/materials", token, "POST", body, null);

        assertEquals(409, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void editorPreviewsSubmitsAndReviewerApprovesMaterial() throws Exception {
        String editorToken = extractJsonString(loginAs("cms-editor").body(), "token", "test-token-cms-editor");
        String reviewerToken = extractJsonString(loginAs("legal-reviewer").body(), "token", "test-token-legal-reviewer");

        HttpResponse<String> preview = sendAuthorized("/api/admin-cms/materials/00000000-0000-0000-0000-000000000027/preview", editorToken, "POST", "{}", null);
        assertEquals(200, preview.statusCode());
        assertContains(preview.body(), "renderModel");
        assertContains(preview.body(), "spring-campaign-editorial");

        HttpResponse<String> submit = sendAuthorized("/api/admin-cms/materials/00000000-0000-0000-0000-000000000027/submit-review", editorToken, "POST", "{}", null);
        assertEquals(200, submit.statusCode());
        assertContains(submit.body(), "IN_REVIEW");

        HttpResponse<String> approve = sendAuthorized("/api/admin-cms/materials/00000000-0000-0000-0000-000000000027/review", reviewerToken, "POST", "{\"decision\":\"APPROVED\",\"comment\":\"Approved for publication\"}", null);
        assertEquals(200, approve.statusCode());
        assertContains(approve.body(), "STR_MNEMO_ADMIN_CMS_REVIEW_APPROVED");
    }

    @Test
    void adminPublishesAndRollsBackVersionWithAudit() throws Exception {
        String token = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");

        HttpResponse<String> publish = sendAuthorized("/api/admin-cms/materials/00000000-0000-0000-0000-000000000027/publish", token, "POST", "{\"publishAt\":null,\"unpublishAt\":null}", "CMS-027-ADMIN");
        assertEquals(200, publish.statusCode());
        assertContains(publish.body(), "PUBLISHED");
        assertContains(publish.body(), "STR_MNEMO_ADMIN_CMS_MATERIAL_PUBLISHED");

        HttpResponse<String> versions = getAuthorized("/api/admin-cms/materials/00000000-0000-0000-0000-000000000027/versions", token);
        assertEquals(200, versions.statusCode());
        assertContains(versions.body(), "versionNumber");

        HttpResponse<String> rollback = sendAuthorized("/api/admin-cms/materials/00000000-0000-0000-0000-000000000027/versions/00000000-0000-0000-0000-000000000127/rollback", token, "POST", "{}", "CMS-027-ADMIN");
        assertEquals(201, rollback.statusCode());
        assertContains(rollback.body(), "STR_MNEMO_ADMIN_CMS_VERSION_ROLLED_BACK");

        HttpResponse<String> audit = getAuthorized("/api/admin-cms/audit?materialId=00000000-0000-0000-0000-000000000027", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "ADMIN_CMS_MATERIAL_PUBLISHED");
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("privateStoragePath"));
    }

    @Test
    void documentWithoutAttachmentReturnsDocumentInvalidMnemonic() throws Exception {
        String token = extractJsonString(loginAs("cms-editor").body(), "token", "test-token-cms-editor");
        String body = "{\"materialType\":\"DOCUMENT\",\"language\":\"ru\",\"slug\":\"terms-of-sale-invalid\",\"title\":\"Terms\",\"audience\":\"PUBLIC\",\"blocks\":[{\"blockType\":\"DOCUMENT_LINK\",\"sortOrder\":1,\"payload\":{\"label\":\"Terms\"}}],\"seo\":{\"slug\":\"terms-of-sale-invalid\",\"title\":\"Terms\",\"description\":\"Terms\",\"canonicalUrl\":\"https://bestorigin.com/documents/terms\",\"robotsPolicy\":\"index,follow\",\"breadcrumbTitle\":\"Terms\"},\"document\":{\"documentType\":\"TERMS_OF_SALE\",\"versionLabel\":\"2026.04\",\"required\":true}}";

        HttpResponse<String> response = sendAuthorized("/api/admin-cms/materials", token, "POST", body, null);

        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID");
    }

    @Test
    void userWithoutCmsScopeIsForbidden() throws Exception {
        String token = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");

        HttpResponse<String> response = getAuthorized("/api/admin-cms/materials", token);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_CMS_ACCESS_DENIED");
        assertNoHardcodedRussianUiText(response.body());
    }

    public void assertFeatureGreenPath() throws Exception {
        contentAdminCreatesMaterialAndReceivesMnemonicContract();
        duplicateSlugReturnsConflictMnemonic();
        editorPreviewsSubmitsAndReviewerApprovesMaterial();
        adminPublishesAndRollsBackVersionWithAudit();
        documentWithoutAttachmentReturnsDocumentInvalidMnemonic();
        userWithoutCmsScopeIsForbidden();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String elevatedSessionId) throws IOException, InterruptedException {
        return send(path, method, body, token, elevatedSessionId);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String elevatedSessionId) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (elevatedSessionId != null) {
            builder.header("X-Elevated-Session-Id", elevatedSessionId);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = builder.method(method, publisher).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Доступ запрещен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Материал сохранен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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