package com.bestorigin.monolith.mlmstructure.impl.service;

import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmConversionFunnelResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmDashboardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmPartnerNodeResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmTeamActivityItemResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmUpgradeRequirementResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmUpgradeResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmVolume;
import com.bestorigin.monolith.mlmstructure.domain.MlmStructureRepository;
import com.bestorigin.monolith.mlmstructure.domain.MlmStructureSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMlmStructureRepository implements MlmStructureRepository {

    private final ConcurrentMap<String, MlmStructureSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public MlmStructureSnapshot findOrCreate(String leaderPersonNumber, String campaignId) {
        return snapshots.computeIfAbsent(campaignId + ":" + leaderPersonNumber, key -> seed(campaignId));
    }

    @Override
    public Optional<MlmStructureSnapshot> findByPartnerPersonNumber(String personNumber, String campaignId) {
        MlmStructureSnapshot snapshot = findOrCreate("BOG-016-001", campaignId);
        boolean exists = snapshot.partners().stream().anyMatch(partner -> partner.personNumber().equals(personNumber));
        return exists ? Optional.of(snapshot) : Optional.empty();
    }

    private static MlmStructureSnapshot seed(String campaignId) {
        MlmDashboardResponse dashboard = new MlmDashboardResponse(
                campaignId,
                "BOG-016-001",
                volume("4200.00"),
                volume("38400.00"),
                7,
                "SILVER",
                "GOLD",
                percent("72.50"),
                List.of("FOCUS_BRANCH_SKINCARE", "SUPPORT_RISK_PARTNER", "CLOSE_FIRST_ORDER_GAP"),
                "STR_MNEMO_MLM_STRUCTURE_DASHBOARD_READY"
        );
        MlmConversionFunnelResponse conversion = new MlmConversionFunnelResponse(campaignId, 42, 31, 24, 18, 14, percent("33.33"), "STR_MNEMO_MLM_STRUCTURE_CONVERSION_READY");
        MlmUpgradeResponse upgrade = new MlmUpgradeResponse(
                campaignId,
                "BOG-016-001",
                "SILVER",
                "GOLD",
                percent("72.50"),
                "2026-05-21T20:59:59Z",
                List.of(
                        new MlmUpgradeRequirementResponse("PERSONAL_VOLUME", "DONE", amount("4200.00"), amount("3000.00"), "STR_MNEMO_MLM_STRUCTURE_REQUIREMENT_DONE"),
                        new MlmUpgradeRequirementResponse("GROUP_VOLUME", "OPEN", amount("38400.00"), amount("50000.00"), "STR_MNEMO_MLM_STRUCTURE_REQUIREMENT_OPEN"),
                        new MlmUpgradeRequirementResponse("ACTIVE_LEGS", "OPEN", amount("2"), amount("3"), "STR_MNEMO_MLM_STRUCTURE_REQUIREMENT_OPEN")
                ),
                "STR_MNEMO_MLM_STRUCTURE_UPGRADE_READY"
        );
        MlmStructureSnapshot snapshot = new MlmStructureSnapshot(dashboard, conversion, upgrade);
        snapshot.partners().add(new MlmPartnerNodeResponse("BOG-016-001", "Марина Лидер", "ROOT", 0, "LEADER", "ACTIVE", volume("4200.00"), volume("38400.00"), percent("0.10")));
        snapshot.partners().add(new MlmPartnerNodeResponse("BOG-016-002", "Анна Скинкеа", "BRANCH-SKINCARE", 2, "CONSULTANT", "ACTIVE", volume("2300.00"), volume("12800.00"), percent("0.18")));
        snapshot.partners().add(new MlmPartnerNodeResponse("BOG-016-003", "Ольга Риск", "BRANCH-MAKEUP", 2, "CONSULTANT", "AT_RISK", volume("350.00"), volume("1800.00"), percent("0.86")));
        snapshot.partners().add(new MlmPartnerNodeResponse("BOG-016-004", "Ирина Апгрейд", "BRANCH-SKINCARE", 3, "MANAGER", "NEW", volume("1200.00"), volume("4100.00"), percent("0.24")));
        snapshot.activities().add(new MlmTeamActivityItemResponse("BOG-016-002", "FIRST_ORDER", "DONE", "2026-05-04T10:15:00Z", false, "STR_MNEMO_MLM_STRUCTURE_ACTIVITY_READY"));
        snapshot.activities().add(new MlmTeamActivityItemResponse("BOG-016-003", "RISK_SIGNAL", "OPEN", "2026-05-06T08:30:00Z", true, "STR_MNEMO_MLM_STRUCTURE_RISK_SIGNAL"));
        snapshot.activities().add(new MlmTeamActivityItemResponse("BOG-016-004", "UPGRADE_PROGRESS", "OPEN", "2026-05-09T12:20:00Z", false, "STR_MNEMO_MLM_STRUCTURE_ACTIVITY_READY"));
        return snapshot;
    }

    private static MlmVolume volume(String value) {
        return new MlmVolume(amount(value), "RUB");
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value).setScale(value.contains(".") ? 2 : 0, RoundingMode.HALF_UP);
    }

    private static BigDecimal percent(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }
}
