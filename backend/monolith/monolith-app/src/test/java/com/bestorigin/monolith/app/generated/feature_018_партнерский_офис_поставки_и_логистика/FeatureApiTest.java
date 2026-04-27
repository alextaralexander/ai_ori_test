// GENERATED FROM agents/tests/. DO NOT EDIT MANUALLY.
package com.bestorigin.tests.feature018;

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
    void partnerOfficeSeesAllOrders() throws Exception {
        String userContextId = loginAs("partner-office");

        HttpResponse<String> response = getAuthorized("/api/partner-office/orders?campaignId=CAT-2026-05", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-018-001");
        assertContains(response.body(), "BOG-SUP-018-001");
        assertContains(response.body(), "\"deliveryStatus\"");
    }

    @Test
    void partnerOfficeFiltersDeviationOrdersBySupply() throws Exception {
        String userContextId = loginAs("partner-office");

        HttpResponse<String> response = getAuthorized("/api/partner-office/orders?supplyId=BOG-SUP-018-001&hasDeviation=true", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-ORD-018-002");
        assertContains(response.body(), "\"hasDeviation\":true");
    }

    @Test
    void partnerOfficeOpensSupplyDetails() throws Exception {
        String userContextId = loginAs("partner-office");

        HttpResponse<String> response = getAuthorized("/api/partner-office/supply/BOG-SUP-018-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"supplyId\":\"BOG-SUP-018-001\"");
        assertContains(response.body(), "\"movements\"");
        assertContains(response.body(), "\"availableActions\"");
    }

    @Test
    void partnerOfficeOpensOrderInsideSupply() throws Exception {
        String userContextId = loginAs("partner-office");

        HttpResponse<String> response = getAuthorized("/api/partner-office/supply/orders/BOG-ORD-018-001", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"orderNumber\":\"BOG-ORD-018-001\"");
        assertContains(response.body(), "\"workflowLinks\"");
    }

    @Test
    void logisticsOperatorTransitionsSupply() throws Exception {
        String userContextId = loginAs("logistics-operator");

        HttpResponse<String> response = postAuthorized("/api/partner-office/supply/BOG-SUP-018-001/transition", userContextId, "{\"targetStatus\":\"ARRIVED\",\"reasonCode\":\"WMS_ARRIVAL_CONFIRMED\"}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFICE_SUPPLY_STATUS_UPDATED");
    }

    @Test
    void partnerOfficeRecordsDeviation() throws Exception {
        String userContextId = loginAs("partner-office");

        HttpResponse<String> response = postAuthorized("/api/partner-office/supply/orders/BOG-ORD-018-002/deviations", userContextId, "{\"supplyId\":\"BOG-SUP-018-001\",\"deviationType\":\"SHORTAGE\",\"sku\":\"SKU-018-LIP-001\",\"quantity\":1,\"reasonCode\":\"SHORT_PACKED\"}");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED");
    }

    @Test
    void regionalManagerSeesOfficeReport() throws Exception {
        String userContextId = loginAs("regional-manager");

        HttpResponse<String> response = getAuthorized("/api/partner-office/report?regionId=REG-018-MSK", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"supplyCount\"");
        assertContains(response.body(), "\"acceptanceSlaPercent\"");
    }

    @Test
    void foreignOfficeCannotOpenSupply() throws Exception {
        String userContextId = loginAs("partner-office-foreign");

        HttpResponse<String> response = getAuthorized("/api/partner-office/supply/BOG-SUP-018-001", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        partnerOfficeSeesAllOrders();
        partnerOfficeFiltersDeviationOrdersBySupply();
        partnerOfficeOpensSupplyDetails();
        partnerOfficeOpensOrderInsideSupply();
        logisticsOperatorTransitionsSupply();
        partnerOfficeRecordsDeviation();
        regionalManagerSeesOfficeReport();
        foreignOfficeCannotOpenSupply();
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

    private HttpResponse<String> postAuthorized(String path, String userContextId, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", "feature-018-" + System.nanoTime())
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
