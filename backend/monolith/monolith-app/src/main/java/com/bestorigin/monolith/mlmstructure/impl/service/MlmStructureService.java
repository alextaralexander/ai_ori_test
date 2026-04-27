package com.bestorigin.monolith.mlmstructure.impl.service;

import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmCommunityResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmConversionFunnelResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmDashboardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmPartnerCardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmTeamActivityResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmUpgradeResponse;

public interface MlmStructureService {

    MlmDashboardResponse dashboard(String userContextId, String campaignId);

    MlmCommunityResponse community(String userContextId, String campaignId, Integer level, String branchId, String status);

    MlmConversionFunnelResponse conversion(String userContextId, String campaignId);

    MlmTeamActivityResponse teamActivity(String userContextId, String campaignId, boolean riskOnly);

    MlmUpgradeResponse upgrade(String userContextId, String campaignId);

    MlmPartnerCardResponse partnerCard(String userContextId, String personNumber, String campaignId);
}
