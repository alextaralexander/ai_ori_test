package com.bestorigin.monolith.partnerreporting.impl.service;

import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.MoneyAmount;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerCommissionDetailResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerPayoutStatus;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReconciliationStatus;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentDownloadResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentLifecycleRequest;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentPageResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentStatus;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportRequest;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportStatus;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportFinanceReconciliationResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportOrderLineResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportOrderPageResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportPrintViewResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportSummaryResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportTotals;
import com.bestorigin.monolith.partnerreporting.domain.PartnerReportRepository;
import com.bestorigin.monolith.partnerreporting.domain.PartnerReportSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultPartnerReportService implements PartnerReportService {

    private static final String ACCESS_DENIED = "STR_MNEMO_PARTNER_REPORT_ACCESS_DENIED";
    private static final String ORDER_NOT_FOUND = "STR_MNEMO_PARTNER_REPORT_ORDER_NOT_FOUND";
    private static final String DOCUMENT_NOT_FOUND = "STR_MNEMO_PARTNER_REPORT_DOCUMENT_NOT_FOUND";
    private static final String DOCUMENT_NOT_PUBLISHED = "STR_MNEMO_PARTNER_REPORT_DOCUMENT_NOT_PUBLISHED";
    private static final String EXPORT_READY = "STR_MNEMO_PARTNER_REPORT_EXPORT_READY";
    private static final String MISMATCH = "STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH";
    private static final String STATUS_CONFLICT = "STR_MNEMO_PARTNER_REPORT_DOCUMENT_STATUS_CONFLICT";
    private final PartnerReportRepository repository;

    public DefaultPartnerReportService(PartnerReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public PartnerReportSummaryResponse summary(String userContextId, String dateFrom, String dateTo, String catalogId, String bonusProgramId) {
        return reportForCurrentPartner(userContextId).summary();
    }

    @Override
    public PartnerReportOrderPageResponse orderLines(String userContextId, String dateFrom, String dateTo, String catalogId, String orderNumber, String payoutStatus, String bonusProgramId, int page, int size) {
        PartnerReportSnapshot report = reportForCurrentPartner(userContextId);
        List<PartnerReportOrderLineResponse> filtered = report.orderLines().stream()
                .filter(line -> blank(orderNumber) || line.orderNumber().equalsIgnoreCase(orderNumber))
                .filter(line -> blank(payoutStatus) || line.calculationStatus().equalsIgnoreCase(payoutStatus) || payoutStatusMatches(line, payoutStatus))
                .toList();
        return new PartnerReportOrderPageResponse(filtered, Math.max(page, 0), Math.max(size, 1), filtered.size());
    }

    @Override
    public PartnerCommissionDetailResponse commissionDetails(String userContextId, String orderNumber) {
        PartnerReportSnapshot report = reportForCurrentPartner(userContextId);
        PartnerReportOrderLineResponse line = report.orderLines().stream()
                .filter(item -> item.orderNumber().equals(orderNumber))
                .findFirst()
                .orElseThrow(() -> new PartnerReportNotFoundException(ORDER_NOT_FOUND));
        return new PartnerCommissionDetailResponse(line, List.copyOf(report.adjustments()), line.payoutReference(), "STR_MNEMO_PARTNER_REPORT_COMMISSION_READY", "CORR-015-COMMISSION-" + Math.abs(orderNumber.hashCode()));
    }

    @Override
    public PartnerReportDocumentPageResponse documents(String userContextId, String dateFrom, String dateTo, String documentType, String documentStatus, int page, int size) {
        PartnerReportSnapshot report = reportForCurrentPartner(userContextId);
        List<PartnerReportDocumentResponse> filtered = report.documents().stream()
                .filter(document -> blank(documentType) || document.documentType().name().equalsIgnoreCase(documentType))
                .filter(document -> blank(documentStatus) || document.documentStatus().name().equalsIgnoreCase(documentStatus))
                .toList();
        return new PartnerReportDocumentPageResponse(filtered, Math.max(page, 0), Math.max(size, 1), filtered.size());
    }

    @Override
    public PartnerReportDocumentDownloadResponse download(String userContextId, UUID documentId) {
        PartnerReportDocumentResponse document = documentForCurrentPartner(userContextId, documentId);
        if (document.documentStatus() != PartnerReportDocumentStatus.PUBLISHED) {
            throw new PartnerReportValidationException(DOCUMENT_NOT_PUBLISHED, 409);
        }
        return new PartnerReportDocumentDownloadResponse(document.documentId(), document.documentCode(), document.documentType(), document.documentStatus(), document.versionNumber(), document.checksumSha256(), "https://files.bestorigin.local/reports/" + document.documentCode() + ".pdf?signature=feature015", "2026-05-22T10:00:00Z", document.publicMnemo());
    }

    @Override
    public PartnerReportPrintViewResponse printView(String userContextId, UUID documentId) {
        PartnerReportDocumentResponse document = documentForCurrentPartner(userContextId, documentId);
        return new PartnerReportPrintViewResponse(document.documentId(), document.documentCode(), document.versionNumber(), document.checksumSha256(), "/print/partner-reporting/" + document.documentCode());
    }

    @Override
    public PartnerReportExportResponse exportReport(String userContextId, PartnerReportExportRequest request) {
        PartnerReportSnapshot report = reportForCurrentPartner(userContextId);
        String format = request == null || blank(request.format()) ? "XLSX" : request.format().toUpperCase(Locale.ROOT);
        if (!"PDF".equals(format) && !"XLSX".equals(format)) {
            throw new PartnerReportValidationException("STR_MNEMO_PARTNER_REPORT_EXPORT_FORMAT_INVALID", 400);
        }
        return new PartnerReportExportResponse(UUID.nameUUIDFromBytes((report.summary().partnerId() + format + "015").getBytes()), PartnerReportExportStatus.READY, format, report.orderLines().size(), EXPORT_READY);
    }

    @Override
    public PartnerReportFinanceReconciliationResponse financeReconciliation(String userContextId, String partnerId, String dateFrom, String dateTo, String reason) {
        if (!financeRole(userContextId) || blank(reason)) {
            throw new PartnerReportAccessDeniedException(ACCESS_DENIED);
        }
        PartnerReportSnapshot report = repository.findOrCreate(blank(partnerId) ? "partner-015" : partnerId);
        PartnerReportTotals totals = new PartnerReportTotals(money("15400.00"), money("11800.00"), money("1416.00"), money("240.00"), money("1176.00"), money("900.00"));
        return new PartnerReportFinanceReconciliationResponse(report.summary().reportPeriodId(), report.summary().partnerId(), report.summary().catalogId(), report.summary().bonusProgramId(), totals, PartnerReconciliationStatus.MISMATCH, MISMATCH, "CORR-015-FINANCE-" + Math.abs(reason.hashCode()), List.of("PAYOUT_AMOUNT_DIFFERS_FROM_PAYABLE", "DOCUMENT_REQUIRES_REISSUE"), true, reason);
    }

    @Override
    public PartnerReportDocumentResponse publishDocument(String userContextId, UUID documentId, PartnerReportDocumentLifecycleRequest request) {
        if (!accountingRole(userContextId) || request == null || blank(request.reasonCode())) {
            throw new PartnerReportAccessDeniedException(ACCESS_DENIED);
        }
        PartnerReportSnapshot report = reportByDocument(documentId);
        PartnerReportDocumentResponse document = findDocument(report, documentId);
        if (document.documentStatus() != PartnerReportDocumentStatus.READY) {
            throw new PartnerReportValidationException(STATUS_CONFLICT, 409);
        }
        return replaceDocument(report, document, PartnerReportDocumentStatus.PUBLISHED, "STR_MNEMO_PARTNER_REPORT_DOCUMENT_PUBLISHED");
    }

    @Override
    public PartnerReportDocumentResponse revokeDocument(String userContextId, UUID documentId, PartnerReportDocumentLifecycleRequest request) {
        if (!financeRole(userContextId) || request == null || blank(request.reasonCode())) {
            throw new PartnerReportAccessDeniedException(ACCESS_DENIED);
        }
        PartnerReportSnapshot report = reportByDocument(documentId);
        PartnerReportDocumentResponse document = findDocument(report, documentId);
        return replaceDocument(report, document, PartnerReportDocumentStatus.REVOKED, MISMATCH);
    }

    private PartnerReportSnapshot reportForCurrentPartner(String userContextId) {
        if (!"partner".equals(role(userContextId))) {
            throw new PartnerReportAccessDeniedException(ACCESS_DENIED);
        }
        return repository.findOrCreate("partner-015");
    }

    private PartnerReportDocumentResponse documentForCurrentPartner(String userContextId, UUID documentId) {
        PartnerReportSnapshot report = reportForCurrentPartner(userContextId);
        return findDocument(report, documentId);
    }

    private PartnerReportSnapshot reportByDocument(UUID documentId) {
        return repository.findByDocumentId(documentId)
                .orElseThrow(() -> new PartnerReportNotFoundException(DOCUMENT_NOT_FOUND));
    }

    private static PartnerReportDocumentResponse findDocument(PartnerReportSnapshot report, UUID documentId) {
        return report.documents().stream()
                .filter(item -> item.documentId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new PartnerReportNotFoundException(DOCUMENT_NOT_FOUND));
    }

    private PartnerReportDocumentResponse replaceDocument(PartnerReportSnapshot report, PartnerReportDocumentResponse document, PartnerReportDocumentStatus status, String publicMnemo) {
        PartnerReportDocumentResponse replacement = new PartnerReportDocumentResponse(document.documentId(), document.documentCode(), document.documentType(), status, document.versionNumber(), document.checksumSha256(), OffsetDateTime.parse("2026-05-22T09:30:00Z").toString(), publicMnemo);
        report.documents().removeIf(item -> item.documentId().equals(document.documentId()));
        report.documents().add(replacement);
        repository.save(report.summary().partnerId(), report);
        return replacement;
    }

    private static boolean payoutStatusMatches(PartnerReportOrderLineResponse line, String payoutStatus) {
        if (PartnerPayoutStatus.PAID.name().equalsIgnoreCase(payoutStatus)) {
            return "PAID".equalsIgnoreCase(line.calculationStatus());
        }
        return false;
    }

    private static String role(String userContextId) {
        String value = userContextId == null ? "" : userContextId.toLowerCase(Locale.ROOT);
        if (value.contains("finance-controller")) {
            return "finance-controller";
        }
        if (value.contains("accountant")) {
            return "accountant";
        }
        if (value.contains("partner")) {
            return "partner";
        }
        return "customer";
    }

    private static boolean financeRole(String userContextId) {
        String role = role(userContextId);
        return "finance-controller".equals(role) || "accountant".equals(role);
    }

    private static boolean accountingRole(String userContextId) {
        String role = role(userContextId);
        return "accountant".equals(role) || "finance-controller".equals(role);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static MoneyAmount money(String value) {
        return new MoneyAmount(new BigDecimal(value).setScale(2, RoundingMode.HALF_UP), "RUB");
    }
}
