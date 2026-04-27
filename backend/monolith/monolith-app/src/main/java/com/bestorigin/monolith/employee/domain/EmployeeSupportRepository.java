package com.bestorigin.monolith.employee.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeSupportRepository {

    EmployeeSupportSnapshot save(EmployeeSupportSnapshot snapshot);

    Optional<EmployeeSupportSnapshot> findOperatorOrder(UUID operatorOrderId);

    List<EmployeeSupportSnapshot> findEscalations();
}
