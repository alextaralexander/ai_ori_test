// GENERATED FROM agents/tests/api/feature_033_админ_заказы_платежи_и_дозаказы/FeatureApiTest.java. DO NOT EDIT MANUALLY.
package com.bestorigin.tests.feature033;

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
    void orderAdminFindsOrderOpensCardAndCreatesSupplementaryOrder() throws Exception {
        String token = extractJsonString(loginAs("order-admin").body(), "token", "test-token-order-admin");

        HttpResponse<String> search = getAuthorized("/api/admin/orders/orders?search=BO-033-1001&orderStatus=DELIVERED&paymentStatus=PAID", token);
        assertEquals(200, search.statusCode());
        assertContains(search.body(), "BO-033-1001");

        String orderId = extractJsonString(search.body(), "orderId", "00000000-0000-0000-0000-000000000033");
        HttpResponse<String> card = getAuthorized("/api/admin/orders/orders/" + orderId, token);
        assertEquals(200, card.statusCode());
        assertContains(card.body(), "paymentEvents");
        assertContains(card.body(), "auditEvents");
        assertFalse(card.body().contains("fullCardPan"));

        String supplementaryBody = "{\"reasonCode\":\"CUSTOMER_REQUEST\",\"comment\":\"Запрошен клиентом после основного заказа\",\"lines\":[{\"sku\":\"BOG-SERUM-001\",\"quantity\":1}]}";
        HttpResponse<String> supplementary = sendAuthorized("/api/admin/orders/orders/" + orderId + "/supplementary-orders", token, "POST", supplementaryBody, "ORDER-033-SUPPLEMENTARY");
        assertEquals(201, supplementary.statusCode());
        assertContains(supplementary.body(), "parentOrderId");
        assertContains(supplementary.body(), "STR_MNEMO_ADMIN_ORDER_SUPPLEMENTARY_CREATED");
        assertNoHardcodedRussianUiText(supplementary.body());
    }

    @Test
    void paymentEventIsIdempotentAndPartialRefundRespectsLimit() throws Exception {
        String token = extractJsonString(loginAs("finance-operator").body(), "token", "test-token-finance-operator");
        String orderId = "00000000-0000-0000-0000-000000000133";
        String paymentBody = "{\"orderId\":\"" + orderId + "\",\"providerCode\":\"PAYMENT_PROVIDER\",\"externalPaymentId\":\"EXT-PAY-033-1\",\"operationType\":\"CAPTURE\",\"amount\":12000.00,\"currencyCode\":\"RUB\",\"payloadChecksum\":\"sha256-033\"}";

        HttpResponse<String> firstPayment = sendAuthorized("/api/admin/orders/payment-events", token, "POST", paymentBody, "PAY-033-CAPTURE");
        HttpResponse<String> duplicatePayment = sendAuthorized("/api/admin/orders/payment-events", token, "POST", paymentBody, "PAY-033-CAPTURE");
        assertEquals(202, firstPayment.statusCode());
        assertEquals(202, duplicatePayment.statusCode());
        assertEquals(extractJsonString(firstPayment.body(), "correlationId", "missing"), extractJsonString(duplicatePayment.body(), "correlationId", "different"));
        assertContains(duplicatePayment.body(), "duplicate");

        String refundBody = "{\"refundType\":\"PARTIAL\",\"amount\":2500.00,\"currencyCode\":\"RUB\",\"reasonCode\":\"PARTIAL_CANCEL\",\"comment\":\"Частичная отмена позиции\"}";
        HttpResponse<String> refund = sendAuthorized("/api/admin/orders/orders/" + orderId + "/refunds", token, "POST", refundBody, "REFUND-033-PARTIAL");
        assertEquals(202, refund.statusCode());
        assertContains(refund.body(), "REQUESTED");

        String invalidRefundBody = "{\"refundType\":\"PARTIAL\",\"amount\":999999.00,\"currencyCode\":\"RUB\",\"reasonCode\":\"OVER_LIMIT\"}";
        HttpResponse<String> invalidRefund = sendAuthorized("/api/admin/orders/orders/" + orderId + "/refunds", token, "POST", invalidRefundBody, "REFUND-033-OVER-LIMIT");
        assertEquals(409, invalidRefund.statusCode());
        assertContains(invalidRefund.body(), "STR_MNEMO_ADMIN_ORDER_FINANCIAL_INVARIANT_FAILED");
        assertNoHardcodedRussianUiText(invalidRefund.body());
    }

    @Test
    void riskDecisionAndFinancialHoldGateSupplementaryOrderActions() throws Exception {
        String financeToken = extractJsonString(loginAs("finance-operator").body(), "token", "test-token-finance-operator");
        String fraudToken = extractJsonString(loginAs("fraud-admin").body(), "token", "test-token-fraud-admin");
        String orderId = "00000000-0000-0000-0000-000000000233";

        String holdBody = "{\"reasonCode\":\"PAYMENT_REVIEW\",\"comment\":\"Проверка платежа\"}";
        HttpResponse<String> hold = sendAuthorized("/api/admin/orders/orders/" + orderId + "/financial-holds", financeToken, "POST", holdBody, "HOLD-033-CREATE");
        assertEquals(201, hold.statusCode());
        assertContains(hold.body(), "ACTIVE");

        String supplementaryBody = "{\"reasonCode\":\"CUSTOMER_REQUEST\",\"lines\":[{\"sku\":\"BOG-SERUM-001\",\"quantity\":1}]}";
        HttpResponse<String> blocked = sendAuthorized("/api/admin/orders/orders/" + orderId + "/supplementary-orders", financeToken, "POST", supplementaryBody, "ORDER-033-BLOCKED-SUPPLEMENTARY");
        assertEquals(409, blocked.statusCode());
        assertContains(blocked.body(), "STR_MNEMO_ADMIN_ORDER_FORBIDDEN_ACTION");

        String riskBody = "{\"riskEventId\":\"00000000-0000-0000-0000-000000000333\",\"decisionStatus\":\"APPROVED\",\"reasonCode\":\"MANUAL_REVIEW_PASSED\",\"comment\":\"Проверка пройдена\"}";
        HttpResponse<String> risk = sendAuthorized("/api/admin/orders/orders/" + orderId + "/risk-decisions", fraudToken, "POST", riskBody, "RISK-033-APPROVE");
        assertEquals(200, risk.statusCode());
        assertContains(risk.body(), "APPROVED");

        String holdId = extractJsonString(hold.body(), "financialHoldId", "00000000-0000-0000-0000-000000000433");
        String releaseBody = "{\"reasonCode\":\"PAYMENT_REVIEW_PASSED\",\"comment\":\"Ручная проверка завершена\"}";
        HttpResponse<String> release = sendAuthorized("/api/admin/orders/financial-holds/" + holdId + "/release", financeToken, "POST", releaseBody, "HOLD-033-RELEASE");
        assertEquals(200, release.statusCode());
        assertContains(release.body(), "RELEASED");
    }

    @Test
    void auditExportAndForbiddenAccessUseMnemonicContract() throws Exception {
        String auditToken = extractJsonString(loginAs("audit-admin").body(), "token", "test-token-audit-admin");
        HttpResponse<String> audit = getAuthorized("/api/admin/orders/audit-events?entityType=ORDER&entityId=BO-033-1001", auditToken);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("secret"));
        assertFalse(audit.body().contains("stackTrace"));

        String exportBody = "{\"format\":\"CSV\",\"filters\":{\"entityType\":\"ORDER\",\"entityId\":\"BO-033-1001\"}}";
        HttpResponse<String> export = sendAuthorized("/api/admin/orders/audit-events/export", auditToken, "POST", exportBody, "AUDIT-033-EXPORT");
        assertEquals(202, export.statusCode());
        assertContains(export.body(), "ACCEPTED");

        String forbiddenToken = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/orders/orders", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_ORDER_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        orderAdminFindsOrderOpensCardAndCreatesSupplementaryOrder();
        paymentEventIsIdempotentAndPartialRefundRespectsLimit();
        riskDecisionAndFinancialHoldGateSupplementaryOrderActions();
        auditExportAndForbiddenAccessUseMnemonicContract();
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
        assertFalse(body.contains("Дозаказ создан"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Действие запрещено"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Финансовая проверка не пройдена"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
