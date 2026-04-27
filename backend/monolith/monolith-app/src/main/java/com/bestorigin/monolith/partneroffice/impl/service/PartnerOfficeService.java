package com.bestorigin.monolith.partneroffice.impl.service;

import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeActionResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeDeviationCreateRequest;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeOrderPageResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeReportResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyDetailsResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyOrderDetailsResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyPageResponse;
import com.bestorigin.monolith.partneroffice.api.PartnerOfficeDtos.PartnerOfficeSupplyTransitionRequest;

public interface PartnerOfficeService {

    PartnerOfficeOrderPageResponse searchOrders(String userContextId, String campaignId, String officeId, String regionId, String query, String supplyId, Boolean hasDeviation, int page, int size);

    PartnerOfficeSupplyPageResponse searchSupply(String userContextId, String officeId, String regionId, String status, Boolean hasDeviation, int page, int size);

    PartnerOfficeSupplyDetailsResponse getSupply(String userContextId, String supplyId);

    PartnerOfficeSupplyOrderDetailsResponse getSupplyOrder(String userContextId, String orderNumber);

    PartnerOfficeActionResponse transitionSupply(String userContextId, String supplyId, PartnerOfficeSupplyTransitionRequest request, String idempotencyKey);

    PartnerOfficeActionResponse recordDeviation(String userContextId, String orderNumber, PartnerOfficeDeviationCreateRequest request, String idempotencyKey);

    PartnerOfficeReportResponse report(String userContextId, String officeId, String regionId);
}
