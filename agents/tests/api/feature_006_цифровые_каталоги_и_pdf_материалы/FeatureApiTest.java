package com.bestorigin.tests.feature006;

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
    void customerCanOpenCurrentDigitalCatalogueWithPagesMaterialsAndHotspots() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = get("/api/catalog/digital-catalogues/current?audience=CUSTOMER");

        assertTrue(userContextId.startsWith("customer-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"issueCode\"");
        assertContains(response.body(), "\"period\"");
        assertContains(response.body(), "\"pages\"");
        assertContains(response.body(), "\"materials\"");
        assertContains(response.body(), "\"hotspots\"");
        assertContains(response.body(), "\"productCode\":\"BOG-CREAM-001\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void customerCanOpenNextDigitalCatalogueWhenPreviewIsAllowed() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = get("/api/catalog/digital-catalogues/next?audience=CUSTOMER");

        assertTrue(userContextId.startsWith("customer-api-session"));
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"periodType\":\"NEXT\"");
        assertContains(response.body(), "\"materials\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void guestCannotOpenRoleRestrictedNextCatalogue() throws Exception {
        String userContextId = loginAs("guest");

        HttpResponse<String> response = get("/api/catalog/digital-catalogues/next?audience=GUEST&preview=false");

        assertTrue(userContextId.startsWith("guest-api-session"));
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN\"");
        assertFalse(response.body().contains("Exception"));
    }

    @Test
    void customerCanCreateDownloadUrlForAllowedMaterial() throws Exception {
        String userContextId = loginAs("customer");
        String body = """
                {"audience":"CUSTOMER","userContextId":"%s","returnUrl":"/products/digital-catalogue-current"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/catalog/digital-catalogues/materials/catalog-current-pdf/download", body);

        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"url\"");
        assertContains(response.body(), "\"expiresAt\"");
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_READY\"");
    }

    @Test
    void unavailableMaterialReturnsMnemonicError() throws Exception {
        String userContextId = loginAs("customer");
        String body = """
                {"audience":"CUSTOMER","userContextId":"%s","returnUrl":"/products/digital-catalogue-current"}
                """.formatted(userContextId);

        HttpResponse<String> response = post("/api/catalog/digital-catalogues/materials/missing-material/download", body);

        assertEquals(404, response.statusCode());
        assertContains(response.body(), "\"messageCode\":\"STR_MNEMO_DIGITAL_CATALOGUE_MATERIAL_UNAVAILABLE\"");
        assertFalse(response.body().contains("Exception"));
    }

    public void assertFeatureGreenPath() throws Exception {
        customerCanOpenCurrentDigitalCatalogueWithPagesMaterialsAndHotspots();
        customerCanOpenNextDigitalCatalogueWhenPreviewIsAllowed();
        guestCannotOpenRoleRestrictedNextCatalogue();
        customerCanCreateDownloadUrlForAllowedMaterial();
        unavailableMaterialReturnsMnemonicError();
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
