package com.bestorigin.monolith.partnerreporting.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PartnerReportingDtos {

    private PartnerReportingDtos() {
    }

    public enum PartnerReconciliationStatus {
        MATCHED,
        MISMATCH,
        PENDING
    }

    public enum PartnerPayoutStatus {
        SCHEDULED,
        PAID,
        FAILED,
        REVERSED
    }

    public enum PartnerReportDocumentType {
        ACT,
        RECEIPT,
        CERTIFICATE,
        PAYOUT_STATEMENT,
        TAX_NOTE,
        RECONCILIATION_REPORT
    }

    public enum PartnerReportDocumentStatus {
        DRAFT,
        READY,
        PUBLISHED,
        REVOKED
    }

    public enum PartnerReportExportStatus {
        REQUESTED,
        READY,
        FAILED,
        EXPIRED
    }

    public record MoneyAmount(BigDecimal amount, String currencyCode) {
    }

    public record PartnerReportTotals(
            MoneyAmount grossSales,
            MoneyAmount commissionBase,
            MoneyAmount accruedCommission,
            MoneyAmount withheld,
            MoneyAmount payable,
            MoneyAmount paid
    ) {
    }

    public record PartnerReportSummaryResponse(
            UUID reportPeriodId,
            String partnerId,
            String catalogId,
            String bonusProgramId,
            PartnerReportTotals totals,
            PartnerReconciliationStatus reconciliationStatus,
            String publicMnemo,
            String correlationId
    ) {
    }

    public record PartnerReportOrderPageResponse(
            List<PartnerReportOrderLineResponse> items,
            int page,
            int size,
            long totalElements
    ) {
    }

    public record PartnerReportOrderLineResponse(
            String orderNumber,
            String orderSource,
            Integer structureLevel,
            String orderedAt,
            MoneyAmount orderAmount,
            MoneyAmount commissionBase,
            BigDecimal commissionRatePercent,
            MoneyAmount commissionAmount,
            String calculationStatus,
            String payoutReference
    ) {
    }

    public record PartnerCommissionAdjustmentResponse(
            String adjustmentType,
            String reasonCode,
            String sourceRef,
            MoneyAmount amount
    ) {
    }

    public record PartnerCommissionDetailResponse(
            PartnerReportOrderLineResponse orderLine,
            List<PartnerCommissionAdjustmentResponse> adjustments,
            String payoutReference,
            String publicMnemo,
            String correlationId
    ) {
    }

    public record PartnerReportDocumentPageResponse(
            List<PartnerReportDocumentResponse> items,
            int page,
            int size,
            long totalElements
    ) {
    }

    public record PartnerReportDocumentResponse(
            UUID documentId,
            String documentCode,
            PartnerReportDocumentType documentType,
            PartnerReportDocumentStatus documentStatus,
            int versionNumber,
            String checksumSha256,
            String publishedAt,
            String publicMnemo
    ) {
    }

    public record PartnerReportDocumentDownloadResponse(
            UUID documentId,
            String documentCode,
            PartnerReportDocumentType documentType,
            PartnerReportDocumentStatus documentStatus,
            int versionNumber,
            String checksumSha256,
            String downloadUrl,
            String expiresAt,
            String publicMnemo
    ) {
    }

    public record PartnerReportPrintViewResponse(
            UUID documentId,
            String documentCode,
            int versionNumber,
            String checksumSha256,
            String printViewUrl
    ) {
    }

    public record PartnerReportExportRequest(
            String format,
            String dateFrom,
            String dateTo,
            String catalogId,
            String bonusProgramId
    ) {
    }

    public record PartnerReportExportResponse(
            UUID exportId,
            PartnerReportExportStatus exportStatus,
            String format,
            int rowCount,
            String publicMnemo
    ) {
    }

    public record PartnerReportFinanceReconciliationResponse(
            UUID reportPeriodId,
            String partnerId,
            String catalogId,
            String bonusProgramId,
            PartnerReportTotals totals,
            PartnerReconciliationStatus reconciliationStatus,
            String publicMnemo,
            String correlationId,
            List<String> mismatchReasons,
            boolean auditRecorded,
            String reason
    ) {
    }

    public record PartnerReportDocumentLifecycleRequest(
            String reasonCode,
            String comment
    ) {
    }

    public record PartnerReportErrorResponse(
            String code,
            List<PartnerReportValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }

    public record PartnerReportValidationReasonResponse(
            String code,
            String severity,
            String target
    ) {
    }
}
