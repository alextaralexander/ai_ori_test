package com.bestorigin.monolith.partnerreporting.domain;

import java.util.Optional;
import java.util.UUID;

public interface PartnerReportRepository {

    PartnerReportSnapshot findOrCreate(String partnerId);

    PartnerReportSnapshot save(String partnerId, PartnerReportSnapshot snapshot);

    Optional<PartnerReportSnapshot> findByPartnerId(String partnerId);

    Optional<PartnerReportSnapshot> findByDocumentId(UUID documentId);
}
