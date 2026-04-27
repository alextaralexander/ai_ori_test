package com.bestorigin.monolith.mlmstructure.domain;

import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmConversionFunnelResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmDashboardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmPartnerNodeResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmTeamActivityItemResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmUpgradeResponse;
import java.util.ArrayList;
import java.util.List;

public class MlmStructureSnapshot {

    private final MlmDashboardResponse dashboard;
    private final MlmConversionFunnelResponse conversion;
    private final MlmUpgradeResponse upgrade;
    private final List<MlmPartnerNodeResponse> partners = new ArrayList<>();
    private final List<MlmTeamActivityItemResponse> activities = new ArrayList<>();

    public MlmStructureSnapshot(MlmDashboardResponse dashboard, MlmConversionFunnelResponse conversion, MlmUpgradeResponse upgrade) {
        this.dashboard = dashboard;
        this.conversion = conversion;
        this.upgrade = upgrade;
    }

    public MlmDashboardResponse dashboard() {
        return dashboard;
    }

    public MlmConversionFunnelResponse conversion() {
        return conversion;
    }

    public MlmUpgradeResponse upgrade() {
        return upgrade;
    }

    public List<MlmPartnerNodeResponse> partners() {
        return partners;
    }

    public List<MlmTeamActivityItemResponse> activities() {
        return activities;
    }
}
