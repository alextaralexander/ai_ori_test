package com.bestorigin.tests.feature014;

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
    void customerSeesWalletSummaryAndBuckets() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/bonus-wallet/summary?type=all", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"balances\"");
        assertContains(response.body(), "\"bucket\":\"CASHBACK\"");
        assertContains(response.body(), "\"availableAmount\"");
        assertContains(response.body(), "\"expiringSoonAmount\"");
    }

    @Test
    void customerFiltersTransactionsByBucketAndOrder() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/bonus-wallet/transactions?type=CASHBACK&orderNumber=ORD-011-MAIN", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "TXN-014-CASHBACK-ACCRUAL");
        assertContains(response.body(), "\"orderNumber\":\"ORD-011-MAIN\"");
        assertContains(response.body(), "STR_MNEMO_BONUS_WALLET_ACCRUAL_ACTIVE");
    }

    @Test
    void customerOpensTransactionDetailsWithLinkedOrder() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/bonus-wallet/transactions/TXN-014-CASHBACK-ACCRUAL", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"transactionId\":\"TXN-014-CASHBACK-ACCRUAL\"");
        assertContains(response.body(), "\"linkedOrderUrl\":\"/order/order-history/ORD-011-MAIN\"");
        assertContains(response.body(), "\"events\"");
    }

    @Test
    void partnerSeesReferralTransactions() throws Exception {
        String userContextId = loginAs("partner");

        HttpResponse<String> response = getAuthorized("/api/bonus-wallet/transactions?type=REFERRAL_DISCOUNT", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"bucket\":\"REFERRAL_DISCOUNT\"");
        assertContains(response.body(), "\"sourceType\":\"REFERRAL\"");
    }

    @Test
    void orderApplyLimitExcludesHoldAndExpiredOperations() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/bonus-wallet/limits/order/ORD-011-MAIN", userContextId);
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"orderNumber\":\"ORD-011-MAIN\"");
        assertContains(response.body(), "\"maxApplicableAmount\"");
        assertFalse(response.body().contains("\"blocked\":true"));
    }

    @Test
    void exportReturnsReadyMetadata() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = postAuthorized("/api/bonus-wallet/exports", "{\"campaignId\":\"CMP-2026-05\",\"format\":\"CSV\"}", userContextId, userContextId + "-export-001");
        assertEquals(200, response.statusCode());
        assertContains(response.body(), "\"status\":\"READY\"");
        assertContains(response.body(), "STR_MNEMO_BONUS_WALLET_EXPORT_READY");
    }

    @Test
    void financeOperatorCreatesManualAdjustmentIdempotently() throws Exception {
        String userContextId = loginAs("finance");
        String body = "{\"targetUserId\":\"customer-014\",\"bucket\":\"MANUAL_ADJUSTMENT\",\"amount\":150.00,\"reasonCode\":\"SERVICE_COMPENSATION\"}";

        HttpResponse<String> first = postAuthorized("/api/bonus-wallet/finance/adjustments", body, userContextId, "feature-014-adjustment-001");
        HttpResponse<String> second = postAuthorized("/api/bonus-wallet/finance/adjustments", body, userContextId, "feature-014-adjustment-001");
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertContains(first.body(), "STR_MNEMO_BONUS_WALLET_ADJUSTMENT_CREATED");
        assertContains(first.body(), "\"auditRecorded\":true");
        assertContains(second.body(), "\"transactionId\":\"TXN-014-MANUAL-feature-014-adjustment-001\"");
    }

    @Test
    void nonFinanceUserCannotOpenForeignWallet() throws Exception {
        String userContextId = loginAs("customer");

        HttpResponse<String> response = getAuthorized("/api/bonus-wallet/finance/customer-014-other?reason=CHECK", userContextId);
        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_BONUS_WALLET_ACCESS_DENIED");
    }

    public void assertFeatureGreenPath() throws Exception {
        customerSeesWalletSummaryAndBuckets();
        customerFiltersTransactionsByBucketAndOrder();
        customerOpensTransactionDetailsWithLinkedOrder();
        partnerSeesReferralTransactions();
        orderApplyLimitExcludesHoldAndExpiredOperations();
        exportReturnsReadyMetadata();
        financeOperatorCreatesManualAdjustmentIdempotently();
        nonFinanceUserCannotOpenForeignWallet();
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

    private HttpResponse<String> postAuthorized(String path, String body, String userContextId, String idempotencyKey) throws IOException, InterruptedException {
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
