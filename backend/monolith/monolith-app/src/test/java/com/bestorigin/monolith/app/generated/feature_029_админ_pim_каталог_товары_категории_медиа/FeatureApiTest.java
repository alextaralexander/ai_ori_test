// GENERATED FROM agents/tests/api; DO NOT EDIT MANUALLY.
package com.bestorigin.tests.feature029;

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
    void pimManagerCreatesCategoryAndProductDraft() throws Exception {
        String token = extractJsonString(loginAs("pim-manager").body(), "token", "test-token-pim-manager");
        String categoryBody = "{\"slug\":\"face-care\",\"locale\":\"ru\",\"name\":\"Уход за лицом\",\"audience\":\"CUSTOMER_AND_PARTNER\",\"sortOrder\":10}";

        HttpResponse<String> category = sendAuthorized("/api/admin-pim/categories", token, "POST", categoryBody, "PIM-029-CATEGORY");
        assertEquals(201, category.statusCode());
        assertContains(category.body(), "face-care");
        assertContains(category.body(), "STR_MNEMO_ADMIN_PIM_CATEGORY_SAVED");
        assertNoHardcodedRussianUiText(category.body());

        String categoryId = extractJsonString(category.body(), "categoryId", "00000000-0000-0000-0000-000000000029");
        HttpResponse<String> activated = sendAuthorized("/api/admin-pim/categories/" + categoryId + "/activate", token, "POST", "{}", "PIM-029-CATEGORY");
        assertEquals(200, activated.statusCode());
        assertContains(activated.body(), "ACTIVE");

        String productBody = "{\"sku\":\"BOG-SERUM-001\",\"articleCode\":\"SRM-001\",\"brandCode\":\"BEST_ORI_GIN\",\"locale\":\"ru\",\"name\":\"Сыворотка сияние\",\"description\":\"Glow serum\",\"composition\":\"Water, niacinamide\",\"usageInstructions\":\"Apply daily\",\"restrictions\":\"Avoid eyes\",\"categoryIds\":[\"" + categoryId + "\"],\"tags\":[\"new\",\"vegan\"]}";
        HttpResponse<String> product = sendAuthorized("/api/admin-pim/products", token, "POST", productBody, "PIM-029-PRODUCT");
        assertEquals(201, product.statusCode());
        assertContains(product.body(), "BOG-SERUM-001");
        assertContains(product.body(), "STR_MNEMO_ADMIN_PIM_PRODUCT_SAVED");
    }

    @Test
    void productPublicationRequiresApprovedMainImage() throws Exception {
        String token = extractJsonString(loginAs("pim-manager").body(), "token", "test-token-pim-manager");
        HttpResponse<String> products = getAuthorized("/api/admin-pim/products?search=BOG-SERUM-001", token);
        String productId = extractJsonString(products.body(), "productId", "00000000-0000-0000-0000-000000000129");

        HttpResponse<String> publish = sendAuthorized("/api/admin-pim/products/" + productId + "/publish", token, "POST", "{\"versionComment\":\"Публикация без медиа\"}", "PIM-029-PUBLISH");

        assertEquals(400, publish.statusCode());
        assertContains(publish.body(), "STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED");
        assertContains(publish.body(), "mainImage");
        assertNoHardcodedRussianUiText(publish.body());
    }

    @Test
    void mediaManagerApprovesMainImageAndProductIsPublished() throws Exception {
        String token = extractJsonString(loginAs("pim-manager").body(), "token", "test-token-pim-manager");
        HttpResponse<String> products = getAuthorized("/api/admin-pim/products?search=BOG-SERUM-001", token);
        String productId = extractJsonString(products.body(), "productId", "00000000-0000-0000-0000-000000000129");
        String mediaBody = "{\"usageType\":\"MAIN_IMAGE\",\"fileName\":\"serum-main.jpg\",\"mimeType\":\"image/jpeg\",\"sizeBytes\":2048,\"checksum\":\"sha256:serum-main\",\"altText\":\"Флакон сыворотки Best Ori Gin\",\"locale\":\"ru\",\"fileReferenceId\":\"s3://pim/serum-main.jpg\"}";

        HttpResponse<String> media = sendAuthorized("/api/admin-pim/products/" + productId + "/media", token, "POST", mediaBody, "PIM-029-MEDIA");
        assertEquals(201, media.statusCode());
        assertContains(media.body(), "STR_MNEMO_ADMIN_PIM_MEDIA_SAVED");

        String mediaId = extractJsonString(media.body(), "mediaId", "00000000-0000-0000-0000-000000000229");
        HttpResponse<String> approved = sendAuthorized("/api/admin-pim/media/" + mediaId + "/approve", token, "POST", "{}", "PIM-029-MEDIA");
        assertEquals(200, approved.statusCode());
        assertContains(approved.body(), "APPROVED");

        HttpResponse<String> published = sendAuthorized("/api/admin-pim/products/" + productId + "/publish", token, "POST", "{\"versionComment\":\"Запуск майского каталога\"}", "PIM-029-PUBLISH");
        assertEquals(200, published.statusCode());
        assertContains(published.body(), "PUBLISHED");
        assertContains(published.body(), "STR_MNEMO_ADMIN_PIM_PRODUCT_PUBLISHED");
    }

    @Test
    void importIsIdempotentAndExportReturnsChecksum() throws Exception {
        String token = extractJsonString(loginAs("pim-manager").body(), "token", "test-token-pim-manager");
        String importBody = "{\"jobCode\":\"CATALOG_MAY_2026\",\"dataType\":\"FULL_CATALOG\",\"sourceFileName\":\"catalog-may-2026.xlsx\"}";

        HttpResponse<String> first = sendAuthorized("/api/admin-pim/imports", token, "POST", importBody, "pim-import-may-2026");
        HttpResponse<String> second = sendAuthorized("/api/admin-pim/imports", token, "POST", importBody, "pim-import-may-2026");

        assertEquals(201, first.statusCode());
        assertEquals(201, second.statusCode());
        assertEquals(extractJsonString(first.body(), "importJobId", "missing"), extractJsonString(second.body(), "importJobId", "different"));
        assertContains(first.body(), "STR_MNEMO_ADMIN_PIM_IMPORT_APPLIED");

        HttpResponse<String> export = sendAuthorized("/api/admin-pim/exports", token, "POST", "{\"status\":\"PUBLISHED\"}", "PIM-029-EXPORT");
        assertEquals(201, export.statusCode());
        assertContains(export.body(), "checksum");
        assertContains(export.body(), "STR_MNEMO_ADMIN_PIM_EXPORT_CREATED");
    }

    @Test
    void auditExposesNoSecretsAndForbiddenUserIsRejected() throws Exception {
        String token = extractJsonString(loginAs("pim-manager").body(), "token", "test-token-pim-manager");
        HttpResponse<String> audit = getAuthorized("/api/admin-pim/audit?entityType=PRODUCT", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("privateStoragePath"));
        assertFalse(audit.body().contains("secret"));

        String forbiddenToken = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");
        HttpResponse<String> forbidden = getAuthorized("/api/admin-pim/workspace", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_PIM_FORBIDDEN");
    }

    public void assertFeatureGreenPath() throws Exception {
        pimManagerCreatesCategoryAndProductDraft();
        productPublicationRequiresApprovedMainImage();
        mediaManagerApprovesMainImageAndProductIsPublished();
        importIsIdempotentAndExportReturnsChecksum();
        auditExposesNoSecretsAndForbiddenUserIsRejected();
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
        assertFalse(body.contains("Доступ запрещен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Товар сохранен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Категория сохранена"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
