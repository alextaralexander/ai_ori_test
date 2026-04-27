package com.bestorigin.monolith.mlmstructure.impl.service;

import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmCommunityResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmConversionFunnelResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmDashboardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmPartnerCardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmPartnerNodeResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmTeamActivityItemResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmTeamActivityResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmUpgradeResponse;
import com.bestorigin.monolith.mlmstructure.domain.MlmStructureRepository;
import com.bestorigin.monolith.mlmstructure.domain.MlmStructureSnapshot;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DefaultMlmStructureService implements MlmStructureService {

    private static final String ACCESS_DENIED = "STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED";
    private static final String FILTER_INVALID = "STR_MNEMO_MLM_STRUCTURE_FILTER_INVALID";
    private static final String PARTNER_NOT_FOUND = "STR_MNEMO_MLM_STRUCTURE_PARTNER_NOT_FOUND";
    private final MlmStructureRepository repository;

    public DefaultMlmStructureService(MlmStructureRepository repository) {
        this.repository = repository;
    }

    @Override
    public MlmDashboardResponse dashboard(String userContextId, String campaignId) {
        assertAllowed(userContextId);
        return snapshot(campaignId).dashboard();
    }

    @Override
    public MlmCommunityResponse community(String userContextId, String campaignId, Integer level, String branchId, String status) {
        assertAllowed(userContextId);
        if (level != null && level < 0) {
            throw new MlmStructureValidationException(FILTER_INVALID);
        }
        List<MlmPartnerNodeResponse> partners = snapshot(campaignId).partners().stream()
                .filter(item -> level == null || item.structureLevel() == level)
                .filter(item -> blank(branchId) || item.branchId().equalsIgnoreCase(branchId))
                .filter(item -> blank(status) || item.partnerStatus().equalsIgnoreCase(status))
                .toList();
        return new MlmCommunityResponse(defaultCampaign(campaignId), partners, partners.size(), "STR_MNEMO_MLM_STRUCTURE_COMMUNITY_READY");
    }

    @Override
    public MlmConversionFunnelResponse conversion(String userContextId, String campaignId) {
        assertAllowed(userContextId);
        return snapshot(campaignId).conversion();
    }

    @Override
    public MlmTeamActivityResponse teamActivity(String userContextId, String campaignId, boolean riskOnly) {
        assertAllowed(userContextId);
        List<MlmTeamActivityItemResponse> items = snapshot(campaignId).activities().stream()
                .filter(item -> !riskOnly || item.riskSignal())
                .toList();
        return new MlmTeamActivityResponse(defaultCampaign(campaignId), items, items.size(), "STR_MNEMO_MLM_STRUCTURE_ACTIVITY_READY");
    }

    @Override
    public MlmUpgradeResponse upgrade(String userContextId, String campaignId) {
        assertAllowed(userContextId);
        return snapshot(campaignId).upgrade();
    }

    @Override
    public MlmPartnerCardResponse partnerCard(String userContextId, String personNumber, String campaignId) {
        assertAllowed(userContextId);
        MlmStructureSnapshot snapshot = repository.findByPartnerPersonNumber(personNumber, defaultCampaign(campaignId))
                .orElseThrow(() -> new MlmStructureNotFoundException(PARTNER_NOT_FOUND));
        MlmPartnerNodeResponse partner = snapshot.partners().stream()
                .filter(item -> item.personNumber().equals(personNumber))
                .findFirst()
                .orElseThrow(() -> new MlmStructureNotFoundException(PARTNER_NOT_FOUND));
        return new MlmPartnerCardResponse(
                partner.personNumber(),
                partner.displayName(),
                "BOG-016-001".equals(partner.personNumber()) ? null : "BOG-016-001",
                partner.branchId(),
                partner.structureLevel(),
                partner.partnerRole(),
                partner.partnerStatus(),
                partner.personalVolume(),
                partner.groupVolume(),
                snapshot.upgrade(),
                Map.of(
                        "orders", "/order/order-history?personNumber=" + partner.personNumber(),
                        "bonus", "/profile/transactions/finance/" + partner.personNumber(),
                        "reports", "/report/order-history?partnerId=" + partner.personNumber(),
                        "supply", "/partner-office/supply?personNumber=" + partner.personNumber()
                ),
                "STR_MNEMO_MLM_STRUCTURE_PARTNER_CARD_READY"
        );
    }

    private MlmStructureSnapshot snapshot(String campaignId) {
        return repository.findOrCreate("BOG-016-001", defaultCampaign(campaignId));
    }

    private static void assertAllowed(String userContextId) {
        String role = role(userContextId);
        if (!"partner-leader".equals(role) && !"business-manager".equals(role) && !"mlm-analyst".equals(role)) {
            throw new MlmStructureAccessDeniedException(ACCESS_DENIED);
        }
    }

    private static String role(String userContextId) {
        String value = userContextId == null ? "" : userContextId.toLowerCase(Locale.ROOT);
        if (value.contains("partner-leader")) {
            return "partner-leader";
        }
        if (value.contains("business-manager")) {
            return "business-manager";
        }
        if (value.contains("mlm-analyst")) {
            return "mlm-analyst";
        }
        return "customer";
    }

    private static String defaultCampaign(String campaignId) {
        return blank(campaignId) ? "CAT-2026-05" : campaignId;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
