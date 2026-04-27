package com.bestorigin.monolith.order.impl.service;

import com.bestorigin.monolith.order.domain.OrderCheckoutRepository;
import com.bestorigin.monolith.order.domain.OrderCheckoutSnapshot;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOrderCheckoutRepository implements OrderCheckoutRepository {

    private final Map<UUID, OrderCheckoutSnapshot> checkouts = new ConcurrentHashMap<>();

    @Override
    public Optional<OrderCheckoutSnapshot> findById(UUID checkoutId) {
        return Optional.ofNullable(checkouts.get(checkoutId));
    }

    @Override
    public Optional<OrderCheckoutSnapshot> findByContext(String ownerUserId, String cartId, String checkoutType) {
        return checkouts.values().stream()
                .filter(checkout -> checkout.ownerUserId().equals(ownerUserId))
                .filter(checkout -> checkout.cartId().equals(cartId))
                .filter(checkout -> checkout.checkoutType().name().equals(checkoutType))
                .findFirst();
    }

    @Override
    public OrderCheckoutSnapshot save(OrderCheckoutSnapshot snapshot) {
        checkouts.put(snapshot.id(), snapshot);
        return snapshot;
    }
}
