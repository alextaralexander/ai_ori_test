package com.bestorigin.monolith.adminbonus.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class AdminBonusDtos {

    private AdminBonusDtos() {
    }

    public record AdminBonusErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record BonusRuleRequest(String ruleCode, String ruleType, Integer priority, String currency, BigDecimal rateValue, String validFrom, String validTo) {
    }

    public record BonusRuleResponse(UUID id, String ruleCode, String ruleType, String status, Integer priority, String currency, BigDecimal rateValue, String validFrom, String validTo, int version, String updatedAt, String messageCode) {
    }

    public record BonusRulePage(List<BonusRuleResponse> items, int page, int size, long total) {
    }

    public record BonusPreviewRequest(String testOrderId, String partnerId) {
    }

    public record BonusPreviewResult(BigDecimal calculationBase, BigDecimal expectedAmount, String currency, List<String> appliedRestrictions, String correlationId) {
    }

    public record QualificationRequest(String qualificationCode, Integer levelNumber, BigDecimal personalVolumeThreshold, BigDecimal groupVolumeThreshold, Integer structureDepth) {
    }

    public record QualificationResponse(UUID id, String qualificationCode, Integer levelNumber, BigDecimal personalVolumeThreshold, BigDecimal groupVolumeThreshold, Integer structureDepth, int version, String messageCode) {
    }

    public record CalculationRunRequest(String periodCode, Boolean recalculation) {
    }

    public record CalculationRunResult(UUID calculationId, String periodCode, String status, String correlationId, String messageCode) {
    }

    public record AccrualResponse(UUID id, String periodCode, String partnerId, String ruleCode, String status, BigDecimal amount, String currency, String correlationId) {
    }

    public record AccrualPage(List<AccrualResponse> items, int page, int size, long total) {
    }

    public record PayoutBatchRequest(String periodCode, String currency, String regionCode, String partnerSegment) {
    }

    public record PayoutBatchResponse(UUID id, String batchCode, String periodCode, String status, BigDecimal totalAmount, String currency, String externalId, String correlationId, String messageCode) {
    }

    public record IntegrationEventResponse(String endpointAlias, String status, int retryCount, String checksum, String lastErrorCode, String lastErrorMessageMnemonic, String correlationId, String createdAt) {
    }

    public record IntegrationEventPage(List<IntegrationEventResponse> items, int page, int size, long total) {
    }
}
