/* Managed synchronized artifact from agents/tests/api. Do not edit manually. */
package com.bestorigin.tests.feature034;

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
    void claimOperatorFindsCaseOpensCardAndRequestsCustomerInfo() throws Exception {
        String token = extractJsonString(loginAs("claim-operator").body(), "token", "test-token-claim-operator");

        HttpResponse<String> search = getAuthorized("/api/admin/service/cases?search=CLM-034-1001&caseStatus=NEW&slaStatus=AT_RISK&claimType=DAMAGED_ITEM", token);
        assertEquals(200, search.statusCode());
        assertContains(search.body(), "CLM-034-1001");

        String serviceCaseId = extractJsonString(search.body(), "serviceCaseId", "00000000-0000-0000-0000-000000000034");
        HttpResponse<String> card = getAuthorized("/api/admin/service/cases/" + serviceCaseId, token);
        assertEquals(200, card.statusCode());
        assertContains(card.body(), "messages");
        assertContains(card.body(), "attachments");
        assertContains(card.body(), "auditEvents");
        assertFalse(card.body().contains("fullCardPan"));

        String assignmentBody = "{\"assignmentAction\":\"TAKE\",\"reasonCode\":\"START_PROCESSING\"}";
        HttpResponse<String> assignment = sendAuthorized("/api/admin/service/cases/" + serviceCaseId + "/assignment", token, "POST", assignmentBody, "SERVICE-034-TAKE");
        assertEquals(200, assignment.statusCode());
        assertContains(assignment.body(), "IN_PROGRESS");

        String messageBody = "{\"messageType\":\"CUSTOMER_MESSAGE\",\"visibility\":\"CUSTOMER_VISIBLE\",\"customerVisibleMessageCode\":\"STR_MNEMO_ADMIN_SERVICE_REQUEST_PHOTO\",\"body\":\"request-photo\",\"reasonCode\":\"WAITING_CUSTOMER_DATA\"}";
        HttpResponse<String> message = sendAuthorized("/api/admin/service/cases/" + serviceCaseId + "/messages", token, "POST", messageBody, "SERVICE-034-REQUEST-INFO");
        assertEquals(201, message.statusCode());
        assertContains(message.body(), "STR_MNEMO_ADMIN_SERVICE_REQUEST_PHOTO");
        assertNoHardcodedRussianUiText(message.body());
    }

    @Test
    void refundDecisionRespectsPaidAmountAndMnemonicErrors() throws Exception {
        String token = extractJsonString(loginAs("claim-operator").body(), "token", "test-token-claim-operator");
        String serviceCaseId = "00000000-0000-0000-0000-000000000134";

        String decisionBody = "{\"decisionType\":\"APPROVE_REFUND\",\"reasonCode\":\"DAMAGED_ITEM_CONFIRMED\",\"customerMessageCode\":\"STR_MNEMO_ADMIN_SERVICE_REFUND_APPROVED\",\"comment\":\"confirmed\"}";
        HttpResponse<String> decision = sendAuthorized("/api/admin/service/cases/" + serviceCaseId + "/decisions", token, "POST", decisionBody, "SERVICE-034-REFUND-DECISION");
        assertEquals(202, decision.statusCode());
        assertContains(decision.body(), "APPROVE_REFUND");

        String decisionId = extractJsonString(decision.body(), "decisionId", "00000000-0000-0000-0000-000000000234");
        String refundBody = "{\"amount\":2500.00,\"currencyCode\":\"RUB\",\"paymentReference\":\"PAY-034-1001\",\"reasonCode\":\"DAMAGED_ITEM_CONFIRMED\"}";
        HttpResponse<String> refund = sendAuthorized("/api/admin/service/decisions/" + decisionId + "/refund-actions", token, "POST", refundBody, "SERVICE-034-REFUND");
        assertEquals(202, refund.statusCode());
        assertContains(refund.body(), "refundStatus");

        String invalidRefundBody = "{\"amount\":999999.00,\"currencyCode\":\"RUB\",\"paymentReference\":\"PAY-034-1001\",\"reasonCode\":\"OVER_LIMIT\"}";
        HttpResponse<String> invalidRefund = sendAuthorized("/api/admin/service/decisions/" + decisionId + "/refund-actions", token, "POST", invalidRefundBody, "SERVICE-034-REFUND-OVER-LIMIT");
        assertEquals(409, invalidRefund.statusCode());
        assertContains(invalidRefund.body(), "STR_MNEMO_ADMIN_SERVICE_REFUND_AMOUNT_EXCEEDED");
        assertNoHardcodedRussianUiText(invalidRefund.body());
    }

    @Test
    void replacementAndWmsEventUpdateServiceCase() throws Exception {
        String token = extractJsonString(loginAs("claim-operator").body(), "token", "test-token-claim-operator");
        String serviceCaseId = "00000000-0000-0000-0000-000000000334";
        String decisionBody = "{\"decisionType\":\"APPROVE_REPLACEMENT\",\"reasonCode\":\"WRONG_ITEM_RECEIVED\",\"customerMessageCode\":\"STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_APPROVED\"}";
        HttpResponse<String> decision = sendAuthorized("/api/admin/service/cases/" + serviceCaseId + "/decisions", token, "POST", decisionBody, "SERVICE-034-REPLACEMENT-DECISION");
        assertEquals(202, decision.statusCode());
        String decisionId = extractJsonString(decision.body(), "decisionId", "00000000-0000-0000-0000-000000000434");

        String replacementBody = "{\"sku\":\"BOG-SERUM-001\",\"quantity\":1,\"warehouseId\":\"00000000-0000-0000-0000-000000003401\",\"reasonCode\":\"WRONG_ITEM_RECEIVED\"}";
        HttpResponse<String> replacement = sendAuthorized("/api/admin/service/decisions/" + decisionId + "/replacement-actions", token, "POST", replacementBody, "SERVICE-034-REPLACEMENT");
        assertEquals(202, replacement.statusCode());
        assertContains(replacement.body(), "replacementStatus");

        String wmsBody = "{\"serviceCaseId\":\"" + serviceCaseId + "\",\"sourceSystem\":\"WMS_1C\",\"externalEventId\":\"WMS-034-RETURN-1\",\"eventType\":\"RETURN_RECEIVED\",\"inspectionStatus\":\"DAMAGED_CONFIRMED\",\"payloadChecksum\":\"sha256-034\"}";
        HttpResponse<String> wms = sendAuthorized("/api/admin/service/wms-events", token, "POST", wmsBody, "SERVICE-034-WMS");
        assertEquals(202, wms.statusCode());
        assertContains(wms.body(), "RETURN_RECEIVED");
    }

    @Test
    void supervisorSlaBoardAndAuditExportUseScopes() throws Exception {
        String supervisorToken = extractJsonString(loginAs("service-supervisor").body(), "token", "test-token-service-supervisor");
        HttpResponse<String> board = getAuthorized("/api/admin/service/supervisor/sla-board", supervisorToken);
        assertEquals(200, board.statusCode());
        assertContains(board.body(), "breachedCases");
        assertContains(board.body(), "items");

        String auditToken = extractJsonString(loginAs("audit-admin").body(), "token", "test-token-audit-admin");
        HttpResponse<String> audit = getAuthorized("/api/admin/service/audit-events?entityType=SERVICE_CASE&entityId=CLM-034-1001", auditToken);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("secret"));
        assertFalse(audit.body().contains("stackTrace"));

        String forbiddenToken = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/service/cases", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_SERVICE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        claimOperatorFindsCaseOpensCardAndRequestsCustomerInfo();
        refundDecisionRespectsPaidAmountAndMnemonicErrors();
        replacementAndWmsEventUpdateServiceCase();
        supervisorSlaBoardAndAuditExportUseScopes();
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
        assertFalse(body.contains("Возврат одобрен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Действие запрещено"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Замена создана"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
