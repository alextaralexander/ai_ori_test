package com.bestorigin.monolith.bonuswallet.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BonusWalletDtos {

    private BonusWalletDtos() {
    }

    public enum BonusBucket {
        CASHBACK,
        REFERRAL_DISCOUNT,
        MANUAL_ADJUSTMENT,
        ORDER_REDEMPTION
    }

    public enum BonusOperationType {
        ACCRUAL,
        HOLD,
        REDEMPTION,
        REVERSAL,
        EXPIRE,
        MANUAL_ADJUSTMENT
    }

    public enum BonusTransactionStatus {
        ACTIVE,
        HOLD,
        REDEEMED,
        REVERSED,
        EXPIRED
    }

    public record BonusWalletSummaryResponse(
            UUID walletId,
            String ownerUserId,
            String currencyCode,
            List<BonusWalletBalanceResponse> balances,
            List<BonusWalletTransactionResponse> recentTransactions,
            BonusWalletApplyLimitResponse applicationLimit,
            Boolean auditRecorded
    ) {
    }

    public record BonusWalletBalanceResponse(
            BonusBucket bucket,
            BigDecimal availableAmount,
            BigDecimal holdAmount,
            BigDecimal expiringSoonAmount,
            String currencyCode
    ) {
    }

    public record BonusWalletTransactionPageResponse(
            List<BonusWalletTransactionResponse> items,
            int page,
            int size,
            long totalElements,
            boolean hasNext
    ) {
    }

    public record BonusWalletTransactionResponse(
            String transactionId,
            BonusBucket bucket,
            BonusOperationType operationType,
            BonusTransactionStatus status,
            BigDecimal amount,
            String currencyCode,
            String sourceType,
            String sourceRef,
            String orderNumber,
            String claimId,
            String campaignId,
            String expiresAt,
            String publicMnemo,
            String correlationId,
            String createdAt
    ) {
    }

    public record BonusWalletTransactionDetailsResponse(
            BonusWalletTransactionResponse transaction,
            String linkedOrderUrl,
            String linkedClaimUrl,
            List<BonusWalletEventResponse> events,
            Boolean auditRecorded
    ) {
    }

    public record BonusWalletEventResponse(
            String eventType,
            String publicStatus,
            String sourceSystem,
            String messageMnemo,
            String occurredAt
    ) {
    }

    public record BonusWalletApplyLimitResponse(
            String orderNumber,
            BigDecimal availableAmount,
            BigDecimal maxApplicableAmount,
            boolean blocked,
            String reasonMnemo
    ) {
    }

    public record BonusWalletExportRequest(
            String campaignId,
            String format
    ) {
    }

    public record BonusWalletExportResponse(
            String exportId,
            String status,
            String format,
            int rowsCount,
            String messageMnemo
    ) {
    }

    public record BonusWalletManualAdjustmentRequest(
            String targetUserId,
            BonusBucket bucket,
            BigDecimal amount,
            String reasonCode
    ) {
    }

    public record BonusWalletErrorResponse(
            String code,
            List<BonusWalletValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }

    public record BonusWalletValidationReasonResponse(
            String code,
            String severity,
            String target
    ) {
    }
}
