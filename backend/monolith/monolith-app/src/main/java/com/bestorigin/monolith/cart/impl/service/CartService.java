package com.bestorigin.monolith.cart.impl.service;

import com.bestorigin.monolith.cart.api.CartDtos.AddCartItemRequest;
import com.bestorigin.monolith.cart.api.CartDtos.CartResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartType;
import com.bestorigin.monolith.cart.api.CartDtos.ChangeQuantityRequest;
import com.bestorigin.monolith.cart.api.CartDtos.ShoppingOffersResponse;
import java.util.UUID;

public interface CartService {

    CartResponse getCurrentCart(String userContextId, CartType cartType);

    CartResponse addItem(String userContextId, CartType cartType, AddCartItemRequest request, String idempotencyKey);

    CartResponse changeQuantity(String userContextId, UUID lineId, ChangeQuantityRequest request, String idempotencyKey);

    CartResponse removeLine(String userContextId, UUID lineId, String idempotencyKey);

    ShoppingOffersResponse getShoppingOffers(String userContextId, CartType cartType);

    CartResponse applyOffer(String userContextId, CartType cartType, String offerId, String idempotencyKey);

    CartResponse supportView(String supportContextId, String userId, CartType cartType);
}
