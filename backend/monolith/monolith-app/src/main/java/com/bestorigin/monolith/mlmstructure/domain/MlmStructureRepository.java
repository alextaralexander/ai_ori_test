package com.bestorigin.monolith.mlmstructure.domain;

import java.util.Optional;

public interface MlmStructureRepository {

    MlmStructureSnapshot findOrCreate(String leaderPersonNumber, String campaignId);

    Optional<MlmStructureSnapshot> findByPartnerPersonNumber(String personNumber, String campaignId);
}
