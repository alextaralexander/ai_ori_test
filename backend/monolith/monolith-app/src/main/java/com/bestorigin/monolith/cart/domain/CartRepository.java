package com.bestorigin.monolith.cart.domain;

import com.bestorigin.monolith.cart.api.CartDtos.CartType;
import java.util.Optional;

public interface CartRepository {

    Optional<CartSnapshot> findActiveCart(String ownerUserId, CartType cartType);

    CartSnapshot save(CartSnapshot cart);
}
