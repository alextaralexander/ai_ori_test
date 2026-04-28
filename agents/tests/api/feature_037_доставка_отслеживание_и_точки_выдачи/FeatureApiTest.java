package com.bestorigin.tests.feature037;

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
    void customerSelectsPickupPointDeliveryAndTracksShipment() throws Exception {
        String token = extractJsonString(loginAs("customer").body(), "token", "test-token-customer");
        HttpResponse<String> options = getAuthorized("/api/delivery/options?orderDraftId=00000000-0000-0000-0000-000000000037&city=Moscow", token);
        assertEquals(200, options.statusCode());
        assertContains(options.body(), "PICKUP_POINT");
        assertContains(options.body(), "expectedReceiveAt");

        HttpResponse<String> pickupPoints = getAuthorized("/api/delivery/pickup-points?city=Moscow", token);
        assertEquals(200, pickupPoints.statusCode());
        assertContains(pickupPoints.body(), "storageLimitDays");

        String createBody = "{\"orderId\":\"00000000-0000-0000-0000-000000000137\",\"customerId\":\"00000000-0000-0000-0000-000000000237\",\"deliveryMethod\":\"PICKUP_POINT\",\"pickupPointId\":\"00000000-0000-0000-0000-000000000337\"}";
        HttpResponse<String> shipment = sendAuthorized("/api/delivery/shipments", token, "POST", createBody, "DELIVERY-037-SHIPMENT");
        assertEquals(201, shipment.statusCode());
        assertContains(shipment.body(), "PICKUP_POINT");
        assertContains(shipment.body(), "correlationId");
    }

    @Test
    void partnerReadsTrackingTimelineWithMnemonicStatuses() throws Exception {
        String token = extractJsonString(loginAs("partner").body(), "token", "test-token-partner");
        HttpResponse<String> tracking = getAuthorized("/api/delivery/shipments/00000000-0000-0000-0000-000000000137/tracking", token);
        assertEquals(200, tracking.statusCode());
        assertContains(tracking.body(), "READY_FOR_PICKUP");
        assertContains(tracking.body(), "STR_MNEMO_DELIVERY_READY_FOR_PICKUP");
        assertContains(tracking.body(), "correlationId");
        assertNoHardcodedRussianUiText(tracking.body());
    }

    @Test
    void pickupOwnerAcceptsAndDeliversShipment() throws Exception {
        String token = extractJsonString(loginAs("pickup-owner").body(), "token", "test-token-pickup-owner");
        HttpResponse<String> queue = getAuthorized("/api/delivery/pickup-owner/shipments?status=ARRIVED_AT_PICKUP_POINT", token);
        assertEquals(200, queue.statusCode());
        assertContains(queue.body(), "ARRIVED_AT_PICKUP_POINT");

        HttpResponse<String> accepted = sendAuthorized("/api/delivery/pickup-owner/shipments/00000000-0000-0000-0000-000000000137/accept", token, "POST", "{\"shipmentCode\":\"SHIP-037\",\"occurredAt\":\"2026-04-28T03:00:00Z\"}", "DELIVERY-037-ACCEPT");
        assertEquals(200, accepted.statusCode());
        assertContains(accepted.body(), "READY_FOR_PICKUP");

        HttpResponse<String> delivered = sendAuthorized("/api/delivery/pickup-owner/shipments/00000000-0000-0000-0000-000000000137/deliver", token, "POST", "{\"verificationCode\":\"037037\",\"occurredAt\":\"2026-04-28T04:00:00Z\"}", "DELIVERY-037-DELIVER");
        assertEquals(200, delivered.statusCode());
        assertContains(delivered.body(), "DELIVERED");
    }

    @Test
    void pickupOwnerRecordsPartialDeliveryAndReturnToLogistics() throws Exception {
        String token = extractJsonString(loginAs("pickup-owner").body(), "token", "test-token-pickup-owner");
        String body = "{\"verificationCode\":\"037037\",\"reasonCode\":\"CUSTOMER_PARTIAL_REFUSAL\",\"occurredAt\":\"2026-04-28T04:30:00Z\",\"items\":[{\"orderItemId\":\"00000000-0000-0000-0000-000000000437\",\"sku\":\"BOG-SERUM-001\",\"quantity\":1,\"itemResult\":\"DELIVERED\"},{\"orderItemId\":\"00000000-0000-0000-0000-000000000537\",\"sku\":\"BOG-CREAM-001\",\"quantity\":1,\"itemResult\":\"RETURN_TO_LOGISTICS\"}]}";
        HttpResponse<String> response = sendAuthorized("/api/delivery/pickup-owner/shipments/00000000-0000-0000-0000-000000000137/partial-deliver", token, "POST", body, "DELIVERY-037-PARTIAL");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "PARTIALLY_DELIVERED");
        assertContains(response.body(), "STR_MNEMO_DELIVERY_PARTIAL_DELIVERY_RECORDED");
    }

    @Test
    void deliveryOperatorReceivesInvalidTransitionMnemonic() throws Exception {
        String token = extractJsonString(loginAs("delivery-operator").body(), "token", "test-token-delivery-operator");
        String body = "{\"externalShipmentId\":\"EXT-037\",\"externalEventId\":\"EXT-037-BAD\",\"status\":\"READY_FOR_PICKUP\",\"sourceSystem\":\"DELIVERY_PROVIDER\",\"reasonCode\":\"OUT_OF_SEQUENCE\",\"occurredAt\":\"2026-04-28T05:00:00Z\"}";
        HttpResponse<String> response = sendAuthorized("/api/delivery/integration/status-events", token, "POST", body, "DELIVERY-037-BAD-STATUS");
        assertEquals(409, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_DELIVERY_INVALID_STATUS_TRANSITION");
    }

    public void assertFeatureGreenPath() throws Exception {
        customerSelectsPickupPointDeliveryAndTracksShipment();
        partnerReadsTrackingTimelineWithMnemonicStatuses();
        pickupOwnerAcceptsAndDeliversShipment();
        pickupOwnerRecordsPartialDeliveryAndReturnToLogistics();
        deliveryOperatorReceivesInvalidTransitionMnemonic();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException { return send("/api/auth/test-login", "POST", "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}", null, null); }
    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException { return send(path, "GET", null, token, null); }
    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException { return send(path, method, body, token, idempotencyKey); }
    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path)).header("Accept", "application/json").header("Accept-Language", "ru-RU").header("Content-Type", "application/json; charset=utf-8").header("X-Correlation-Id", "CORR-037");
        if (token != null) { builder.header("Authorization", "Bearer " + token); }
        if (idempotencyKey != null) { builder.header("Idempotency-Key", idempotencyKey); }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        return http.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
    private static void assertContains(String body, String expected) { assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body); }
    private static void assertNoHardcodedRussianUiText(String body) { assertFalse(body.contains("Готов к выдаче")); assertFalse(body.contains("Доступ запрещен")); }
    private static String extractJsonString(String body, String field, String fallback) { String marker = "\"" + field + "\":\""; int start = body.indexOf(marker); if (start < 0) { return fallback; } int valueStart = start + marker.length(); int valueEnd = body.indexOf('"', valueStart); return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback; }
}
