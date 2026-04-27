package com.bestorigin.tests.feature028;

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
    void marketingAdminCreatesLandingVariantAndReceivesMnemonicContract() throws Exception {
        String token = extractJsonString(loginAs("marketing-admin").body(), "token", "test-token-marketing-admin");
        String body = "{\"landingType\":\"BUSINESS\",\"locale\":\"ru\",\"slug\":\"business-partner-spring\",\"name\":\"Business spring\",\"campaignCode\":\"BIZ-SPRING-2026\",\"activeFrom\":\"2026-05-01T00:00:00+03:00\",\"activeTo\":\"2026-05-21T23:59:59+03:00\",\"hero\":{\"heading\":\"Business\"},\"blocks\":[{\"blockType\":\"HERO\",\"sortOrder\":1,\"payload\":{\"heading\":\"Business\"}},{\"blockType\":\"BENEFIT\",\"sortOrder\":2,\"payload\":{\"text\":\"Benefit\"}},{\"blockType\":\"CTA\",\"sortOrder\":3,\"payload\":{\"route\":\"/invite/business-partner-registration\"}},{\"blockType\":\"LEGAL_NOTICE\",\"sortOrder\":4,\"payload\":{\"text\":\"Terms\"}}]}";

        HttpResponse<String> response = sendAuthorized("/api/admin-referral/landing-variants", token, "POST", body, "REF-028-ADMIN");

        assertEquals(201, response.statusCode());
        assertContains(response.body(), "business-partner-spring");
        assertContains(response.body(), "STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void landingWithoutLegalNoticeReturnsValidationMnemonic() throws Exception {
        String token = extractJsonString(loginAs("marketing-admin").body(), "token", "test-token-marketing-admin");
        String body = "{\"landingType\":\"BUSINESS\",\"locale\":\"ru\",\"slug\":\"business-without-legal\",\"name\":\"Invalid\",\"campaignCode\":\"BIZ-SPRING-2026\",\"activeFrom\":\"2026-05-01T00:00:00+03:00\",\"activeTo\":\"2026-05-21T23:59:59+03:00\",\"blocks\":[{\"blockType\":\"HERO\",\"sortOrder\":1,\"payload\":{\"heading\":\"Business\"}},{\"blockType\":\"CTA\",\"sortOrder\":2,\"payload\":{\"route\":\"/invite/business-partner-registration\"}}]}";

        HttpResponse<String> response = sendAuthorized("/api/admin-referral/landing-variants", token, "POST", body, null);

        assertEquals(400, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_REFERRAL_LANDING_LEGAL_NOTICE_REQUIRED");
        assertNoHardcodedRussianUiText(response.body());
    }

    @Test
    void crmAdminConfiguresFunnelAndGeneratesReferralCodeIdempotently() throws Exception {
        String token = extractJsonString(loginAs("crm-admin").body(), "token", "test-token-crm-admin");
        String funnelBody = "{\"funnelCode\":\"business-partner-default\",\"scenario\":\"BUSINESS_PARTNER\",\"steps\":[\"PERSONAL_DATA\",\"CONTACT_CONFIRMATION\",\"PARTNER_TERMS\",\"ACCOUNT_SETUP\"],\"consentCodes\":[\"PERSONAL_DATA_PROCESSING\",\"PARTNER_COMMERCIAL_TERMS\"],\"validationRules\":{\"email\":true},\"defaultContext\":{\"landingType\":\"BUSINESS\"}}";

        HttpResponse<String> funnel = sendAuthorized("/api/admin-referral/funnels", token, "POST", funnelBody, "REF-028-CRM");
        assertEquals(201, funnel.statusCode());
        assertContains(funnel.body(), "STR_MNEMO_ADMIN_REFERRAL_FUNNEL_SAVED");

        HttpResponse<String> activate = sendAuthorized("/api/admin-referral/funnels/00000000-0000-0000-0000-000000000028/activate", token, "POST", "{}", "REF-028-CRM");
        assertEquals(200, activate.statusCode());
        assertContains(activate.body(), "ACTIVE");

        String codeBody = "{\"codeType\":\"CAMPAIGN_MULTI_USE\",\"campaignCode\":\"BIZ-SPRING-2026\",\"landingType\":\"BUSINESS\",\"activeFrom\":\"2026-05-01T00:00:00+03:00\",\"activeTo\":\"2026-05-21T23:59:59+03:00\",\"maxUsageCount\":1000,\"constraints\":{\"channel\":\"partner-link\"}}";
        HttpResponse<String> first = sendAuthorized("/api/admin-referral/referral-codes", token, "POST", codeBody, "REF-028-IDEMPOTENT");
        HttpResponse<String> second = sendAuthorized("/api/admin-referral/referral-codes", token, "POST", codeBody, "REF-028-IDEMPOTENT");

        assertEquals(201, first.statusCode());
        assertEquals(201, second.statusCode());
        assertContains(first.body(), "STR_MNEMO_ADMIN_REFERRAL_CODE_GENERATED");
        assertEquals(extractJsonString(first.body(), "publicCode", "missing"), extractJsonString(second.body(), "publicCode", "different"));
    }

    @Test
    void crmAdminUpdatesAttributionPolicyAndOverridesSponsor() throws Exception {
        String token = extractJsonString(loginAs("crm-admin").body(), "token", "test-token-crm-admin");
        String policyBody = "{\"prioritySources\":[\"URL_REFERRAL_CODE\",\"MANUAL_CODE\",\"SESSION_CONTEXT\",\"CAMPAIGN_DEFAULT_SPONSOR\",\"CRM_OVERRIDE\"],\"conflictStrategy\":\"FIRST_MATCH_WINS\"}";

        HttpResponse<String> policy = sendAuthorized("/api/admin-referral/attribution-policy", token, "PUT", policyBody, "REF-028-CRM");
        assertEquals(200, policy.statusCode());
        assertContains(policy.body(), "STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_POLICY_SAVED");

        String overrideBody = "{\"registrationId\":\"00000000-0000-0000-0000-000000000828\",\"sponsorPartnerId\":\"00000000-0000-0000-0000-000000000168\",\"reasonCode\":\"SPONSOR_CLAIM_ACCEPTED\",\"comment\":\"Checked CRM evidence\"}";
        HttpResponse<String> override = sendAuthorized("/api/admin-referral/attribution/override", token, "POST", overrideBody, "REF-028-CRM");
        assertEquals(200, override.statusCode());
        assertContains(override.body(), "STR_MNEMO_ADMIN_REFERRAL_ATTRIBUTION_OVERRIDDEN");
        assertContains(override.body(), "SPONSOR_CLAIM_ACCEPTED");
    }

    @Test
    void analyticsAndAuditExposeNoSecrets() throws Exception {
        String token = extractJsonString(loginAs("marketing-admin").body(), "token", "test-token-marketing-admin");

        HttpResponse<String> report = getAuthorized("/api/admin-referral/analytics/conversions?campaignCode=BIZ-SPRING-2026", token);
        assertEquals(200, report.statusCode());
        assertContains(report.body(), "LANDING_VIEWED");
        assertContains(report.body(), "PARTNER_ACTIVATED");
        assertFalse(report.body().contains("secret"));
        assertFalse(report.body().contains("token"));

        HttpResponse<String> audit = getAuthorized("/api/admin-referral/audit?entityType=REFERRAL_CODE", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertFalse(audit.body().contains("privateStoragePath"));
    }

    @Test
    void userWithoutAdminReferralScopeIsForbidden() throws Exception {
        String token = extractJsonString(loginAs("employee-support").body(), "token", "test-token-employee-support");

        HttpResponse<String> response = getAuthorized("/api/admin-referral/landing-variants", token);

        assertEquals(403, response.statusCode());
        assertContains(response.body(), "STR_MNEMO_ADMIN_REFERRAL_FORBIDDEN");
        assertNoHardcodedRussianUiText(response.body());
    }

    public void assertFeatureGreenPath() throws Exception {
        marketingAdminCreatesLandingVariantAndReceivesMnemonicContract();
        landingWithoutLegalNoticeReturnsValidationMnemonic();
        crmAdminConfiguresFunnelAndGeneratesReferralCodeIdempotently();
        crmAdminUpdatesAttributionPolicyAndOverridesSponsor();
        analyticsAndAuditExposeNoSecrets();
        userWithoutAdminReferralScopeIsForbidden();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        String body = "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}";
        return send("/api/auth/test-login", "POST", body, null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String elevatedSessionId) throws IOException, InterruptedException {
        return send(path, method, body, token, elevatedSessionId);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String elevatedSessionId) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Accept-Language", "ru-RU")
                .header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (elevatedSessionId != null) {
            builder.header("X-Elevated-Session-Id", elevatedSessionId);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = builder.method(method, publisher).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static void assertNoHardcodedRussianUiText(String body) {
        assertFalse(body.contains("Доступ запрещен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Лендинг сохранен"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
        assertFalse(body.contains("Код создан"), "Backend must return mnemonic codes, not predefined Russian UI text: " + body);
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
