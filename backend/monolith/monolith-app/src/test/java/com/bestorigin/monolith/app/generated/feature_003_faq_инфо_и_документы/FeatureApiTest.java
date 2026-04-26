// Synchronized from agents/tests/. Do not edit this runtime copy manually.
package com.bestorigin.tests.feature003;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class FeatureApiTest {

    private static final String BASE_URL = System.getProperty("bestorigin.baseUrl", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void guestCanSearchFaqAndOpenRelatedInfoSection() throws Exception {
        HttpResponse<String> faq = get("/api/public-content/faq?audience=GUEST&query=" + encode("delivery"));

        assertEquals(200, faq.statusCode());
        assertContains(faq.body(), "\"categoryKey\":\"delivery\"");
        assertContains(faq.body(), "\"questionKey\":\"public.faq.delivery.time.question\"");
        assertContains(faq.body(), "\"relatedInfoSection\":\"delivery\"");

        HttpResponse<String> info = get("/api/public-content/info/delivery?audience=GUEST");

        assertEquals(200, info.statusCode());
        assertContains(info.body(), "\"sectionCode\":\"delivery\"");
        assertContains(info.body(), "\"sectionType\":\"RICH_TEXT\"");
        assertContains(info.body(), "\"documentType\":\"terms\"");
    }

    @Test
    void customerCanLoadTermsDocumentsWithCurrentVersionAndArchive() throws Exception {
        HttpResponse<String> documents = get("/api/public-content/documents/terms?audience=CUSTOMER");

        assertEquals(200, documents.statusCode());
        assertContains(documents.body(), "\"documentType\":\"terms\"");
        assertContains(documents.body(), "\"versionLabel\":\"2.1\"");
        assertContains(documents.body(), "\"current\":true");
        assertContains(documents.body(), "\"viewerUrl\":\"/assets/documents/terms-v2-1.pdf\"");
        assertContains(documents.body(), "\"archive\"");
    }

    @Test
    void partnerReceivesPartnerDocumentsAndPublicMaterials() throws Exception {
        HttpResponse<String> documents = get("/api/public-content/documents/partner?audience=PARTNER");

        assertEquals(200, documents.statusCode());
        assertContains(documents.body(), "\"documentType\":\"partner\"");
        assertContains(documents.body(), "\"required\":true");
        assertContains(documents.body(), "\"audience\":\"PARTNER\"");
        assertContains(documents.body(), "\"audience\":\"ANY\"");
    }

    @Test
    void unavailableDocumentTypeDoesNotRevealDocumentBodies() throws Exception {
        HttpResponse<String> response = get("/api/public-content/documents/unknown?audience=GUEST");

        assertEquals(404, response.statusCode());
        assertContains(response.body(), "\"code\":\"STR_MNEMO_PUBLIC_DOCUMENTS_NOT_FOUND\"");
        assertFalse(response.body().contains("terms-v2-1"));
    }

    public void assertFeatureGreenPath() throws Exception {
        guestCanSearchFaqAndOpenRelatedInfoSection();
        customerCanLoadTermsDocumentsWithCurrentVersionAndArchive();
        partnerReceivesPartnerDocumentsAndPublicMaterials();
        unavailableDocumentTypeDoesNotRevealDocumentBodies();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }
}
