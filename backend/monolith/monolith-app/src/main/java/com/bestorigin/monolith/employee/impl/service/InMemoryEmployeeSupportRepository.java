package com.bestorigin.monolith.employee.impl.service;

import com.bestorigin.monolith.employee.domain.EmployeeSupportRepository;
import com.bestorigin.monolith.employee.domain.EmployeeSupportSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryEmployeeSupportRepository implements EmployeeSupportRepository {

    private final CopyOnWriteArrayList<EmployeeSupportSnapshot> snapshots = new CopyOnWriteArrayList<>();

    @Override
    public EmployeeSupportSnapshot save(EmployeeSupportSnapshot snapshot) {
        snapshots.add(snapshot);
        return snapshot;
    }

    @Override
    public Optional<EmployeeSupportSnapshot> findOperatorOrder(UUID operatorOrderId) {
        return snapshots.stream()
                .filter(snapshot -> operatorOrderId.equals(snapshot.operatorOrderId()))
                .findFirst();
    }

    @Override
    public List<EmployeeSupportSnapshot> findEscalations() {
        List<EmployeeSupportSnapshot> result = new ArrayList<>();
        for (EmployeeSupportSnapshot snapshot : snapshots) {
            if ("ESCALATION".equals(snapshot.actionType())) {
                result.add(snapshot);
            }
        }
        return result;
    }
}
