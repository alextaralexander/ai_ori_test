// Synchronized from agents/tests. Do not edit this generated runtime copy manually.
package com.bestorigin.tests.feature032;

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
    void logisticsAdminCreatesWarehouseChangesAvailabilityAndSeesStock() throws Exception {
        String token = extractJsonString(loginAs("logistics-admin").body(), "token", "test-token-logistics-admin");
        String warehouseBody = "{\"warehouseCode\":\"WH-MSK-01\",\"name\":\"Московский склад\",\"warehouseType\":\"FULFILLMENT\",\"regionCode\":\"MSK\",\"sourceSystem\":\"WMS\",\"externalWarehouseId\":\"WMS-MSK-01\",\"salesChannels\":[\"WEB\",\"PARTNER_OFFICE\"]}";

        HttpResponse<String> warehouse = sendAuthorized("/api/admin/wms/warehouses", token, "POST", warehouseBody, "WMS-032-WAREHOUSE");
        assertEquals(201, warehouse.statusCode());
        assertContains(warehouse.body(), "WH-MSK-01");
        assertContains(warehouse.body(), "STR_MNEMO_ADMIN_WMS_WAREHOUSE_SAVED");
        assertNoHardcodedRussianUiText(warehouse.body());

        HttpResponse<String> stocks = getAuthorized("/api/admin/wms/stocks?sku=BOG-SERUM-001&channelCode=WEB", token);
        assertEquals(200, stocks.statusCode());
        assertContains(stocks.body(), "BOG-SERUM-001");

        String stockItemId = extractJsonString(stocks.body(), "stockItemId", "00000000-0000-0000-0000-000000000032");
        String ruleBody = "{\"policy\":\"SELLABLE\",\"reasonCode\":\"CATALOG_READY\",\"activeFrom\":\"2026-05-01T00:00:00Z\",\"activeTo\":\"2026-05-21T23:59:59Z\"}";
        HttpResponse<String> rule = sendAuthorized("/api/admin/wms/stocks/" + stockItemId + "/availability-rule", token, "POST", ruleBody, "WMS-032-AVAILABILITY");
        assertEquals(200, rule.statusCode());
        assertContains(rule.body(), "SELLABLE");
        assertContains(rule.body(), "STR_MNEMO_ADMIN_WMS_AVAILABILITY_SAVED");
    }

    @Test
    void supplyAcceptanceIsIdempotentAndCreatesDiscrepancy() throws Exception {
        String token = extractJsonString(loginAs("warehouse-operator").body(), "token", "test-token-warehouse-operator");
        String supplyBody = "{\"supplyCode\":\"SUP-032-001\",\"warehouseId\":\"00000000-0000-0000-0000-000000000032\",\"sourceSystem\":\"WMS\",\"externalDocumentId\":\"WMS-DOC-100\",\"expectedAt\":\"2026-05-01T09:00:00Z\",\"lines\":[{\"sku\":\"BOG-SERUM-001\",\"plannedQty\":120,\"externalLineId\":\"1\"}]}";
        HttpResponse<String> supply = sendAuthorized("/api/admin/wms/supplies", token, "POST", supplyBody, "WMS-032-SUPPLY");
        assertEquals(201, supply.statusCode());
        assertContains(supply.body(), "SUP-032-001");

        String supplyId = extractJsonString(supply.body(), "supplyId", "00000000-0000-0000-0000-000000000132");
        String acceptanceBody = "{\"lines\":[{\"sku\":\"BOG-SERUM-001\",\"plannedQty\":120,\"acceptedQty\":118,\"damagedQty\":2,\"shortageQty\":0,\"surplusQty\":0,\"reasonCode\":\"DAMAGED_IN_TRANSIT\"}]}";
        HttpResponse<String> first = sendAuthorized("/api/admin/wms/supplies/" + supplyId + "/acceptance", token, "POST", acceptanceBody, "WMS-032-ACCEPT");
        HttpResponse<String> second = sendAuthorized("/api/admin/wms/supplies/" + supplyId + "/acceptance", token, "POST", acceptanceBody, "WMS-032-ACCEPT");
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertContains(first.body(), "PARTIALLY_ACCEPTED");
        assertContains(first.body(), "STR_MNEMO_ADMIN_WMS_SUPPLY_ACCEPTED");
        assertEquals(extractJsonString(first.body(), "correlationId", "missing"), extractJsonString(second.body(), "correlationId", "different"));
    }

    @Test
    void reservationShortageAndReleaseUseMnemonicContract() throws Exception {
        String token = extractJsonString(loginAs("order-admin").body(), "token", "test-token-order-admin");
        String reservationBody = "{\"orderId\":\"ORDER-032-001\",\"warehouseId\":\"00000000-0000-0000-0000-000000000032\",\"sku\":\"BOG-SERUM-001\",\"quantity\":3}";
        HttpResponse<String> reservation = sendAuthorized("/api/admin/wms/reservations", token, "POST", reservationBody, "WMS-032-RESERVE");
        assertEquals(201, reservation.statusCode());
        assertContains(reservation.body(), "HELD");

        String reservationId = extractJsonString(reservation.body(), "reservationId", "00000000-0000-0000-0000-000000000232");
        HttpResponse<String> release = sendAuthorized("/api/admin/wms/reservations/" + reservationId + "/release", token, "POST", null, "WMS-032-RELEASE");
        assertEquals(200, release.statusCode());
        assertContains(release.body(), "RELEASED");

        String shortageBody = "{\"orderId\":\"ORDER-032-002\",\"warehouseId\":\"00000000-0000-0000-0000-000000000032\",\"sku\":\"BOG-SERUM-001\",\"quantity\":9999}";
        HttpResponse<String> shortage = sendAuthorized("/api/admin/wms/reservations", token, "POST", shortageBody, "WMS-032-SHORTAGE");
        assertEquals(409, shortage.statusCode());
        assertContains(shortage.body(), "STR_MNEMO_ADMIN_WMS_STOCK_NOT_ENOUGH");
        assertNoHardcodedRussianUiText(shortage.body());
    }

    @Test
    void syncDuplicateQuarantineAuditAndForbiddenAreSafe() throws Exception {
        String token = extractJsonString(loginAs("wms-integration-operator").body(), "token", "test-token-wms-integration-operator");
        String syncBody = "{\"sourceSystem\":\"WMS\",\"warehouseId\":\"00000000-0000-0000-0000-000000000032\",\"skuFilter\":\"BOG-SERUM-001\",\"documentType\":\"STOCK_BALANCE\"}";
        HttpResponse<String> sync = sendAuthorized("/api/admin/wms/sync-runs", token, "POST", syncBody, "WMS-032-SYNC");
        assertEquals(202, sync.statusCode());
        assertContains(sync.body(), "STARTED");

        HttpResponse<String> messages = getAuthorized("/api/admin/wms/sync-messages?messageStatus=QUARANTINED", token);
        assertEquals(200, messages.statusCode());
        assertFalse(messages.body().contains("secret"));
        assertFalse(messages.body().contains("stackTrace"));

        HttpResponse<String> audit = getAuthorized("/api/admin/wms/audit-events?entityType=SYNC_MESSAGE", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "ADMIN_WMS");

        String forbiddenToken = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/wms/warehouses", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_WMS_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        logisticsAdminCreatesWarehouseChangesAvailabilityAndSeesStock();
        supplyAcceptanceIsIdempotentAndCreatesDiscrepancy();
        reservationShortageAndReleaseUseMnemonicContract();
        syncDuplicateQuarantineAuditAndForbiddenAreSafe();
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
        assertFalse(body.contains("Склад сохранен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Остатков недостаточно"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
