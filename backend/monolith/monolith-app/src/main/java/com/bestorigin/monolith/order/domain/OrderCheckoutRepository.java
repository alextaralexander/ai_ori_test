package com.bestorigin.monolith.order.domain;

import java.util.Optional;
import java.util.UUID;

public interface OrderCheckoutRepository {

    Optional<OrderCheckoutSnapshot> findById(UUID checkoutId);

    Optional<OrderCheckoutSnapshot> findByContext(String ownerUserId, String cartId, String checkoutType);

    OrderCheckoutSnapshot save(OrderCheckoutSnapshot snapshot);
}
