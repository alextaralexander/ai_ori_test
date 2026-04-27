package com.bestorigin.monolith.cart.impl.service;

import com.bestorigin.monolith.cart.api.CartDtos.CartType;
import com.bestorigin.monolith.cart.domain.CartRepository;
import com.bestorigin.monolith.cart.domain.CartSnapshot;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCartRepository implements CartRepository {

    private final Map<String, CartSnapshot> carts = new ConcurrentHashMap<>();

    @Override
    public Optional<CartSnapshot> findActiveCart(String ownerUserId, CartType cartType) {
        return Optional.ofNullable(carts.get(key(ownerUserId, cartType)));
    }

    @Override
    public CartSnapshot save(CartSnapshot cart) {
        carts.put(key(cart.ownerUserId(), cart.cartType()), cart);
        return cart;
    }

    private static String key(String ownerUserId, CartType cartType) {
        return ownerUserId + ":" + cartType;
    }
}
