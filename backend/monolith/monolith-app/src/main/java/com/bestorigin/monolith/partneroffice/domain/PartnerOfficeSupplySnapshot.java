package com.bestorigin.monolith.partneroffice.domain;

import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeDeviationResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeEscalationResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeMovementResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeOrderSummaryResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyItemResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplySummaryResponse;
import java.util.ArrayList;
import java.util.List;

public class PartnerOfficeSupplySnapshot {

    private final PartnerOfficeSupplySummaryResponse summary;
    private final List<PartnerOfficeOrderSummaryResponse> orders = new ArrayList<>();
    private final List<PartnerOfficeSupplyItemResponse> items = new ArrayList<>();
    private final List<PartnerOfficeMovementResponse> movements = new ArrayList<>();
    private final List<PartnerOfficeDeviationResponse> deviations = new ArrayList<>();
    private final List<PartnerOfficeEscalationResponse> escalations = new ArrayList<>();

    public PartnerOfficeSupplySnapshot(PartnerOfficeSupplySummaryResponse summary) {
        this.summary = summary;
    }

    public PartnerOfficeSupplySummaryResponse summary() {
        return summary;
    }

    public List<PartnerOfficeOrderSummaryResponse> orders() {
        return orders;
    }

    public List<PartnerOfficeSupplyItemResponse> items() {
        return items;
    }

    public List<PartnerOfficeMovementResponse> movements() {
        return movements;
    }

    public List<PartnerOfficeDeviationResponse> deviations() {
        return deviations;
    }

    public List<PartnerOfficeEscalationResponse> escalations() {
        return escalations;
    }
}
