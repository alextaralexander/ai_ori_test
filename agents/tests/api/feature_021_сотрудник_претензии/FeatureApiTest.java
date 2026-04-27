package com.bestorigin.tests.feature021;

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
    void employeeCreatesClaimForCustomerOrder() throws Exception {
        String userContextId = loginAs("employee-support");
        String body = """
                {
                  "customerId":"CUST-021-001",
                  "orderNumber":"BOG-ORD-021-001",
                  "sourceChannel":"PHONE",
                  "supportReasonCode":"CUSTOMER_CALL",
                  "requestedResolution":"REFUND",
                  "comment":"STR_MNEMO_EMPLOYEE_CLAIM_CREATED",
                  "items":[{"sku":"SKU-021-001","productCode":"PRD-021-001","quantity":1,"problemType":"DAMAGED_ITEM","requestedResolution":"REFUND"}],
                  "attachments":[{"fileId":"ATT-021-001","filename":"claim-photo.jpg","mimeType":"image/jpeg","sizeBytes":512000,"accessPolicy":"INTERNAL"}]
                }
                """;

        HttpResponse<String> response = postAuthorized("/api/employee/submit-claim", userContextId, body, "EMP-CLM-021-001");
        assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
        assertContains(response.body(), "BOG-CLM-021-001");
        assertContains(response.body(), "slaState");
        assertContains(response.body(), "compensationAmount");
        assertContains(response.body(), "auditEvents");
    }

    @Test
    void idempotentClaimCreationReturnsExistingCase() throws Exception {
        String userContextId = loginAs("employee-support");
        String body = """
                {"customerId":"CUST-021-001","orderNumber":"BOG-ORD-021-001","sourceChannel":"PHONE","supportReasonCode":"CUSTOMER_CALL","requestedResolution":"REFUND","items":[{"sku":"SKU-021-001","productCode":"PRD-021-001","quantity":1,"problemType":"DAMAGED_ITEM","requestedResolution":"REFUND"}]}
                """;

        HttpResponse<String> first = postAuthorized("/api/employee/submit-claim", userContextId, body, "EMP-CLM-021-IDEMPOTENT");
        HttpResponse<String> second = postAuthorized("/api/employee/submit-claim", userContextId, body, "EMP-CLM-021-IDEMPOTENT");
        assertTrue(first.statusCode() == 200 || first.statusCode() == 201);
        assertEquals(200, second.statusCode());
        assertContains(second.body(), "BOG-CLM-021-001");
    }

    @Test
    void employeeFiltersClaimsBySlaAndStatus() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/claims?claimStatus=IN_REVIEW&slaState=AT_RISK&dateFrom=2026-04-01&dateTo=2026-04-27", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "BOG-CLM-021-001");
        assertContains(response.body(), "maskedContact");
        assertContains(response.body(), "AT_RISK");
    }

    @Test
    void employeeOpensClaimDetails() throws Exception {
        String userContextId = loginAs("employee-support");

        HttpResponse<String> response = getAuthorized("/api/employee/claims/BOG-CLM-021-001?supportReasonCode=CUSTOMER_CALL", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "SKU-021-001");
        assertContains(response.body(), "routeTasks");
        assertContains(response.body(), "auditEvents");
        assertContains(response.body(), "publicReasonMnemonic");
    }

    @Test
    void employeeRoutesClaimToWarehouseAndFinance() throws Exception {
        String userContextId = loginAs("employee-support");
        String warehouseBody = "{\"transitionCode\":\"SEND_TO_WAREHOUSE_REVIEW\",\"supportReasonCode\":\"CUSTOMER_CALL\"}";
        String financeBody = "{\"transitionCode\":\"SEND_TO_FINANCE_REFUND\",\"supportReasonCode\":\"CUSTOMER_CALL\",\"approvedCompensationAmount\":1250.00}";

        HttpResponse<String> warehouse = postAuthorized("/api/employee/claims/BOG-CLM-021-001/transitions", userContextId, warehouseBody, "EMP-CLM-021-WAREHOUSE");
        assertEquals(200, warehouse.statusCode());
        assertContains(warehouse.body(), "WAREHOUSE");

        HttpResponse<String> finance = postAuthorized("/api/employee/claims/BOG-CLM-021-001/transitions", userContextId, financeBody, "EMP-CLM-021-FINANCE");
        assertEquals(200, finance.statusCode());
        assertContains(finance.body(), "FINANCE");
        assertContains(finance.body(), "STR_MNEMO_EMPLOYEE_CLAIM_SENT_TO_FINANCE");
    }

    @Test
    void supervisorApprovesClaimCompensation() throws Exception {
        String userContextId = loginAs("supervisor");
        String body = "{\"transitionCode\":\"APPROVE_COMPENSATION\",\"supportReasonCode\":\"SUPERVISOR_REVIEW\",\"approvedCompensationAmount\":2500.00}";

        HttpResponse<String> response = postAuthorized("/api/employee/claims/BOG-CLM-021-002/transitions", userContextId, body, "EMP-CLM-021-SUPERVISOR");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "EMPLOYEE_CLAIM_SUPERVISOR_APPROVED");
        assertContains(response.body(), "FINANCE");
    }

    @Test
    void invalidClaimReturnsMnemonic() throws Exception {
        String userContextId = loginAs("employee-support");
        String body = "{\"customerId\":\"CUST-021-001\",\"orderNumber\":\"BOG-ORD-021-001\",\"supportReasonCode\":\"CUSTOMER_CALL\",\"requestedResolution\":\"REFUND\",\"items\":[]}";

        HttpResponse<String> response = postAuthorized("/api/employee/submit-claim", userContextId, body, "EMP-CLM-021-INVALID");
        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED");
    }

    @Test
    void partnerCannotOpenEmployeeClaims() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/employee/claims", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_EMPLOYEE_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        employeeCreatesClaimForCustomerOrder();
        idempotentClaimCreationReturnsExistingCase();
        employeeFiltersClaimsBySlaAndStatus();
        employeeOpensClaimDetails();
        employeeRoutesClaimToWarehouseAndFinance();
        supervisorApprovesClaimCompensation();
        invalidClaimReturnsMnemonic();
        partnerCannotOpenEmployeeClaims();
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

    private HttpResponse<String> postAuthorized(String path, String userContextId, String body, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Authorization", "Bearer " + userContextId)
                .header("Idempotency-Key", idempotencyKey)
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
