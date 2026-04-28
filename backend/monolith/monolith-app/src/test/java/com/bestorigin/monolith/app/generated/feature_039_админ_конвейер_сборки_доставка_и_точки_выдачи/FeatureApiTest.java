// AUTO-GENERATED from agents/tests/. Do not edit this synchronized runtime copy manually.
package com.bestorigin.tests.feature039;

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
    void fulfillmentAdminMovesOrderThroughConveyorAndDeliveryTransfer() throws Exception {
        String token = extractJsonString(loginAs("fulfillment-admin").body(), "token", "test-token-fulfillment-admin");
        String taskBody = "{\"taskCode\":\"FUL-039-001\",\"orderId\":\"BO-E2E-039-1\",\"shipmentId\":\"SHP-E2E-039-1\",\"warehouseCode\":\"WH-MSK-01\",\"zoneCode\":\"A1\",\"stage\":\"PICK_PENDING\",\"priority\":39,\"slaDeadlineAt\":\"2026-04-28T12:00:00Z\",\"correlationId\":\"CORR-039-1\"}";
        HttpResponse<String> created = sendAuthorized("/api/admin/fulfillment/tasks", token, "POST", taskBody, "FUL-039-CREATE");
        assertEquals(201, created.statusCode());
        assertContains(created.body(), "FUL-039-001");
        String taskId = extractJsonString(created.body(), "id", "00000000-0000-0000-0000-000000000039");

        assertStage(token, taskId, "PICK_IN_PROGRESS", "FUL-039-PICK");
        assertStage(token, taskId, "PACK_IN_PROGRESS", "FUL-039-PACK");
        assertStage(token, taskId, "SORT_PENDING", "FUL-039-SORT");
        HttpResponse<String> ready = sendAuthorized("/api/admin/fulfillment/tasks/" + taskId + "/stage", token, "POST", "{\"targetStage\":\"READY_TO_SHIP\",\"correlationId\":\"CORR-039-1\"}", "FUL-039-READY");
        assertEquals(200, ready.statusCode());
        assertContains(ready.body(), "READY_TO_SHIP");

        HttpResponse<String> dashboard = getAuthorized("/api/admin/fulfillment/dashboard/shipments?correlationId=CORR-039-1", token);
        assertEquals(200, dashboard.statusCode());
        assertContains(dashboard.body(), "CORR-039-1");
    }

    @Test
    void deliveryAdminCreatesServiceAndPickupNetworkAdminControlsPickupPoint() throws Exception {
        String deliveryToken = extractJsonString(loginAs("delivery-admin").body(), "token", "test-token-delivery-admin");
        String serviceBody = "{\"serviceCode\":\"DELIVERY_039\",\"displayNameKey\":\"adminFulfillment.delivery.delivery039\",\"integrationMode\":\"EXTERNAL_API\",\"endpointAlias\":\"delivery-provider-039\"}";
        HttpResponse<String> service = sendAuthorized("/api/admin/fulfillment/delivery-services", deliveryToken, "POST", serviceBody, "FUL-039-DELIVERY");
        assertEquals(201, service.statusCode());
        String serviceId = extractJsonString(service.body(), "id", "00000000-0000-0000-0000-000000000139");

        String tariffBody = "{\"zoneCode\":\"RU-MSK\",\"deliveryMethod\":\"PICKUP_POINT\",\"currency\":\"RUB\",\"baseAmount\":199.00,\"validFrom\":\"2026-04-28T00:00:00Z\",\"priority\":39}";
        assertEquals(201, sendAuthorized("/api/admin/fulfillment/delivery-services/" + serviceId + "/tariffs", deliveryToken, "POST", tariffBody, "FUL-039-TARIFF").statusCode());
        String slaBody = "{\"zoneCode\":\"RU-MSK\",\"stage\":\"READY_FOR_PICKUP\",\"durationMinutes\":2880}";
        assertEquals(201, sendAuthorized("/api/admin/fulfillment/delivery-services/" + serviceId + "/sla-rules", deliveryToken, "POST", slaBody, "FUL-039-SLA").statusCode());
        HttpResponse<String> activated = sendAuthorized("/api/admin/fulfillment/delivery-services/" + serviceId + "/activate", deliveryToken, "POST", "{}", "FUL-039-ACTIVATE");
        assertEquals(200, activated.statusCode());
        assertContains(activated.body(), "ACTIVE");

        String pickupToken = extractJsonString(loginAs("pickup-network-admin").body(), "token", "test-token-pickup-network-admin");
        String pickupBody = "{\"pickupPointCode\":\"PVZ-039-001\",\"ownerUserId\":\"OWNER-039\",\"addressText\":\"Москва, Тестовая улица, 39\",\"latitude\":55.7558000,\"longitude\":37.6173000,\"storageLimitDays\":7,\"shipmentLimit\":120,\"zoneCodes\":[\"RU-MSK\"]}";
        HttpResponse<String> pickup = sendAuthorized("/api/admin/fulfillment/pickup-points", pickupToken, "POST", pickupBody, "FUL-039-PVZ");
        assertEquals(201, pickup.statusCode());
        String pickupPointId = extractJsonString(pickup.body(), "id", "00000000-0000-0000-0000-000000000239");
        assertEquals(200, sendAuthorized("/api/admin/fulfillment/pickup-points/" + pickupPointId + "/activate", pickupToken, "POST", "{}", "FUL-039-PVZ-ACTIVE").statusCode());

        HttpResponse<String> closed = sendAuthorized("/api/admin/fulfillment/pickup-points/" + pickupPointId + "/temporary-close", pickupToken, "POST", "{\"reasonCode\":\"OVERLOAD\",\"correlationId\":\"CORR-039-PVZ\"}", "FUL-039-PVZ-CLOSE");
        assertEquals(200, closed.statusCode());
        assertContains(closed.body(), "TEMPORARILY_CLOSED");
    }

    @Test
    void pickupOwnerMarksNotCollectedAndTriggersReturnLogistics() throws Exception {
        String token = extractJsonString(loginAs("pickup-owner").body(), "token", "test-token-pickup-owner");
        HttpResponse<String> accepted = sendAuthorized("/api/admin/fulfillment/pickup-shipments/SHP-E2E-039-3/accept", token, "POST", "{}", "FUL-039-ACCEPT");
        assertEquals(200, accepted.statusCode());
        assertContains(accepted.body(), "ACCEPTED");

        HttpResponse<String> notCollected = sendAuthorized("/api/admin/fulfillment/pickup-shipments/SHP-E2E-039-3/not-collected", token, "POST", "{\"reasonCode\":\"STORAGE_EXPIRED\",\"correlationId\":\"CORR-039-RETURN\"}", "FUL-039-NOT-COLLECTED");
        assertEquals(200, notCollected.statusCode());
        assertContains(notCollected.body(), "NOT_COLLECTED");
        assertContains(notCollected.body(), "CORR-039-RETURN");
    }

    public void assertFeatureGreenPath() throws Exception {
        fulfillmentAdminMovesOrderThroughConveyorAndDeliveryTransfer();
        deliveryAdminCreatesServiceAndPickupNetworkAdminControlsPickupPoint();
        pickupOwnerMarksNotCollectedAndTriggersReturnLogistics();
    }

    private void assertStage(String token, String taskId, String targetStage, String idempotencyKey) throws IOException, InterruptedException {
        HttpResponse<String> response = sendAuthorized("/api/admin/fulfillment/tasks/" + taskId + "/stage", token, "POST", "{\"targetStage\":\"" + targetStage + "\",\"correlationId\":\"CORR-039-1\"}", idempotencyKey);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), targetStage);
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "@bestorigin.test\",\"password\":\"test-password\",\"role\":\"" + role + "\"}";
        return send("/api/auth/login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", "", token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException {
        return send(path, method, body, token, idempotencyKey);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        if ("GET".equals(method)) {
            builder.GET();
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static String extractJsonString(String body, String field, String fallback) {
        String marker = "\"" + field + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd < 0 ? fallback : body.substring(valueStart, valueEnd);
    }
}
