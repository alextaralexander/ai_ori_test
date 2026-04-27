package com.bestorigin.monolith.partnerreporting.impl.service;

import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerCommissionDetailResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentDownloadResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentLifecycleRequest;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentPageResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportRequest;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportExportResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportFinanceReconciliationResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportOrderPageResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportPrintViewResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportSummaryResponse;
import java.util.UUID;

public interface PartnerReportService {

    PartnerReportSummaryResponse summary(String userContextId, String dateFrom, String dateTo, String catalogId, String bonusProgramId);

    PartnerReportOrderPageResponse orderLines(String userContextId, String dateFrom, String dateTo, String catalogId, String orderNumber, String payoutStatus, String bonusProgramId, int page, int size);

    PartnerCommissionDetailResponse commissionDetails(String userContextId, String orderNumber);

    PartnerReportDocumentPageResponse documents(String userContextId, String dateFrom, String dateTo, String documentType, String documentStatus, int page, int size);

    PartnerReportDocumentDownloadResponse download(String userContextId, UUID documentId);

    PartnerReportPrintViewResponse printView(String userContextId, UUID documentId);

    PartnerReportExportResponse exportReport(String userContextId, PartnerReportExportRequest request);

    PartnerReportFinanceReconciliationResponse financeReconciliation(String userContextId, String partnerId, String dateFrom, String dateTo, String reason);

    PartnerReportDocumentResponse publishDocument(String userContextId, UUID documentId, PartnerReportDocumentLifecycleRequest request);

    PartnerReportDocumentResponse revokeDocument(String userContextId, UUID documentId, PartnerReportDocumentLifecycleRequest request);
}
