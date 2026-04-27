package com.bestorigin.monolith.partnerreporting.impl.service;

import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.MoneyAmount;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerCommissionAdjustmentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReconciliationStatus;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentStatus;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentType;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportOrderLineResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportSummaryResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportTotals;
import com.bestorigin.monolith.partnerreporting.domain.PartnerReportRepository;
import com.bestorigin.monolith.partnerreporting.domain.PartnerReportSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPartnerReportRepository implements PartnerReportRepository {

    private final ConcurrentMap<String, PartnerReportSnapshot> reports = new ConcurrentHashMap<>();

    @Override
    public PartnerReportSnapshot findOrCreate(String partnerId) {
        return reports.computeIfAbsent(partnerId, InMemoryPartnerReportRepository::seed);
    }

    @Override
    public PartnerReportSnapshot save(String partnerId, PartnerReportSnapshot snapshot) {
        reports.put(partnerId, snapshot);
        return snapshot;
    }

    @Override
    public Optional<PartnerReportSnapshot> findByPartnerId(String partnerId) {
        return Optional.ofNullable(reports.get(partnerId));
    }

    @Override
    public Optional<PartnerReportSnapshot> findByDocumentId(UUID documentId) {
        reports.putIfAbsent("partner-015", seed("partner-015"));
        return reports.values().stream()
                .filter(report -> report.documents().stream().anyMatch(document -> document.documentId().equals(documentId)))
                .findFirst();
    }

    private static PartnerReportSnapshot seed(String partnerId) {
        PartnerReportSummaryResponse summary = new PartnerReportSummaryResponse(
                uuid("report-" + partnerId + "-2026-05"),
                partnerId,
                "CAT-2026-05",
                "MLM-BASE",
                new PartnerReportTotals(
                        money("15400.00"),
                        money("11800.00"),
                        money("1416.00"),
                        money("180.00"),
                        money("1236.00"),
                        money("900.00")
                ),
                PartnerReconciliationStatus.MATCHED,
                "STR_MNEMO_PARTNER_REPORT_READY",
                "CORR-015-" + Math.abs(partnerId.hashCode())
        );
        PartnerReportSnapshot snapshot = new PartnerReportSnapshot(summary);
        snapshot.orderLines().add(new PartnerReportOrderLineResponse("ORD-015-STRUCTURE-001", "STRUCTURE", 2, "2026-05-04T10:15:00Z", money("5400.00"), money("5400.00"), percent("12.0000"), money("648.00"), "READY", "PAYOUT-015-001"));
        snapshot.orderLines().add(new PartnerReportOrderLineResponse("ORD-015-SELF-002", "SELF", 0, "2026-05-05T12:00:00Z", money("2300.00"), money("2300.00"), percent("8.0000"), money("184.00"), "PAID", "PAYOUT-015-001"));
        snapshot.orderLines().add(new PartnerReportOrderLineResponse("ORD-015-RETURN-003", "STRUCTURE", 1, "2026-05-08T09:30:00Z", money("900.00"), money("900.00"), percent("10.0000"), money("90.00"), "HELD", "PAYOUT-015-002"));
        snapshot.adjustments().add(new PartnerCommissionAdjustmentResponse("RETURN", "RET-015-001", "ORD-015-RETURN-003", money("-90.00")));
        snapshot.adjustments().add(new PartnerCommissionAdjustmentResponse("TAX", "MONTHLY_TAX", "PAYOUT-015-001", money("-90.00")));
        snapshot.documents().add(new PartnerReportDocumentResponse(uuid("doc-015-001"), "DOC-015-ACT-001", PartnerReportDocumentType.ACT, PartnerReportDocumentStatus.PUBLISHED, 3, "sha256:015-act-001", "2026-05-22T09:00:00Z", "STR_MNEMO_PARTNER_REPORT_DOCUMENT_PUBLISHED"));
        snapshot.documents().add(new PartnerReportDocumentResponse(UUID.fromString("00000000-0015-0000-0000-000000000001"), "DOC-015-RECEIPT-001", PartnerReportDocumentType.PAYOUT_STATEMENT, PartnerReportDocumentStatus.PUBLISHED, 2, "sha256:015-receipt-001", "2026-05-22T09:10:00Z", "STR_MNEMO_PARTNER_REPORT_DOCUMENT_PUBLISHED"));
        snapshot.documents().add(new PartnerReportDocumentResponse(UUID.fromString("00000000-0015-0000-0000-000000000002"), "DOC-015-ACT-002", PartnerReportDocumentType.ACT, PartnerReportDocumentStatus.READY, 1, "sha256:015-act-002", null, "STR_MNEMO_PARTNER_REPORT_DOCUMENT_READY"));
        snapshot.documents().add(new PartnerReportDocumentResponse(UUID.fromString("00000000-0015-0000-0000-000000000003"), "DOC-015-ACT-003", PartnerReportDocumentType.RECONCILIATION_REPORT, PartnerReportDocumentStatus.PUBLISHED, 1, "sha256:015-act-003", "2026-05-22T09:20:00Z", "STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH"));
        return snapshot;
    }

    private static UUID uuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private static MoneyAmount money(String value) {
        return new MoneyAmount(new BigDecimal(value).setScale(2, RoundingMode.HALF_UP), "RUB");
    }

    private static BigDecimal percent(String value) {
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_UP);
    }
}
