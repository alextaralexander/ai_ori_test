package com.bestorigin.monolith.adminbenefitprogram.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminBenefitProgramDtos {

    private AdminBenefitProgramDtos() {
    }

    public record AdminBenefitProgramErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record BenefitProgramRequest(
            String code,
            String type,
            String catalogId,
            String activeFrom,
            String activeTo,
            String ownerRole,
            Map<String, Object> rules,
            Eligibility eligibility,
            Compatibility compatibility,
            Map<String, Object> lifecycle) {
    }

    public record Eligibility(List<String> roles, BigDecimal minOrderAmount) {
    }

    public record Compatibility(Integer priority, Boolean stackable, Integer maxBenefitsPerOrder) {
    }

    public record BenefitProgramResponse(
            UUID id,
            String code,
            String type,
            String catalogId,
            String status,
            String activeFrom,
            String activeTo,
            String ownerRole,
            Map<String, Object> rules,
            Eligibility eligibility,
            Compatibility compatibility,
            int version,
            String messageCode) {
    }

    public record BenefitProgramPage(List<BenefitProgramResponse> items, int page, int size, long total) {
    }

    public record DryRunRequest(String partnerNumber, String catalogId, String cartId, String scenario) {
    }

    public record DryRunResponse(
            UUID programId,
            boolean applicable,
            String partnerNumber,
            String catalogId,
            String cartId,
            BigDecimal benefitAmount,
            String currency,
            String decisionCode,
            String correlationId) {
    }

    public record ProgramStatusRequest(String targetStatus, String reasonCode, String scheduledAt) {
    }

    public record BudgetRequest(
            String currency,
            BigDecimal totalBudget,
            BigDecimal cashbackLimit,
            BigDecimal discountLimit,
            Integer redemptionLimit,
            Boolean stopOnExhausted) {
    }

    public record BudgetResponse(
            UUID programId,
            String currency,
            BigDecimal totalBudget,
            BigDecimal cashbackLimit,
            BigDecimal discountLimit,
            Integer redemptionLimit,
            boolean stopOnExhausted,
            String status,
            String messageCode) {
    }

    public record ManualAdjustmentRequest(
            String targetPartnerNumber,
            String adjustmentType,
            BigDecimal amount,
            String currency,
            String reasonCode,
            String evidenceRef) {
    }

    public record ManualAdjustmentResponse(
            UUID id,
            UUID programId,
            String targetPartnerNumber,
            String adjustmentType,
            BigDecimal amount,
            String currency,
            String status,
            String reasonCode,
            String evidenceRef,
            String correlationId,
            String messageCode) {
    }

    public record AuditEventResponse(
            UUID id,
            UUID programId,
            String actionCode,
            String actorRole,
            String reasonCode,
            String correlationId,
            String createdAt) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }

    public record IntegrationEventResponse(
            UUID id,
            UUID programId,
            String targetContext,
            String eventType,
            String status,
            String idempotencyKey,
            String correlationId,
            String createdAt,
            String messageCode) {
    }

    public record IntegrationEventPage(List<IntegrationEventResponse> items, int page, int size, long total) {
    }
}
