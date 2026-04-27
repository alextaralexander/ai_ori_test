package com.bestorigin.monolith.partneroffice.domain;

import java.util.Collection;
import java.util.Optional;

public interface PartnerOfficeRepository {

    Collection<PartnerOfficeSupplySnapshot> findAll();

    Optional<PartnerOfficeSupplySnapshot> findBySupplyId(String supplyId);

    Optional<PartnerOfficeSupplySnapshot> findByOrderNumber(String orderNumber);

    void save(PartnerOfficeSupplySnapshot supply);
}
