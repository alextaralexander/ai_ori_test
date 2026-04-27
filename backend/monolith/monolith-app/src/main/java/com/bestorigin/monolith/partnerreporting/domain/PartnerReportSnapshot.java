package com.bestorigin.monolith.partnerreporting.domain;

import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerCommissionAdjustmentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportDocumentResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportOrderLineResponse;
import com.bestorigin.monolith.partnerreporting.api.PartnerReportingDtos.PartnerReportSummaryResponse;
import java.util.ArrayList;
import java.util.List;

public class PartnerReportSnapshot {

    private final PartnerReportSummaryResponse summary;
    private final List<PartnerReportOrderLineResponse> orderLines = new ArrayList<>();
    private final List<PartnerCommissionAdjustmentResponse> adjustments = new ArrayList<>();
    private final List<PartnerReportDocumentResponse> documents = new ArrayList<>();

    public PartnerReportSnapshot(PartnerReportSummaryResponse summary) {
        this.summary = summary;
    }

    public PartnerReportSummaryResponse summary() {
        return summary;
    }

    public List<PartnerReportOrderLineResponse> orderLines() {
        return orderLines;
    }

    public List<PartnerCommissionAdjustmentResponse> adjustments() {
        return adjustments;
    }

    public List<PartnerReportDocumentResponse> documents() {
        return documents;
    }
}
