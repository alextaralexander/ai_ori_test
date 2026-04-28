package com.bestorigin.monolith.partnerbenefits.domain;

import java.util.Optional;
import java.util.UUID;

public interface PartnerBenefitsRepository {
    PartnerBenefitsSnapshot defaultSnapshot();

    Optional<PartnerBenefitsSnapshot> findByPartnerNumber(String partnerNumber);

    void saveRedemption(UUID rewardId, String idempotencyKey, String correlationId);
}
