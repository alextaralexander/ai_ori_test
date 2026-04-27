package com.bestorigin.monolith.cart.domain;

import com.bestorigin.monolith.cart.api.CartDtos.AppliedOfferResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartLineResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartStatus;
import com.bestorigin.monolith.cart.api.CartDtos.CartTotalsResponse;
import com.bestorigin.monolith.cart.api.CartDtos.CartType;
import com.bestorigin.monolith.cart.api.CartDtos.RoleSegment;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartSnapshot {

    private final UUID cartId;
    private final String ownerUserId;
    private final CartType cartType;
    private final String campaignId;
    private final RoleSegment roleSegment;
    private final String partnerContextId;
    private CartStatus status;
    private int version;
    private final List<CartLineResponse> lines = new ArrayList<>();
    private final List<AppliedOfferResponse> appliedOffers = new ArrayList<>();
    private CartTotalsResponse totals;

    public CartSnapshot(UUID cartId, String ownerUserId, CartType cartType, String campaignId, RoleSegment roleSegment, String partnerContextId) {
        this.cartId = cartId;
        this.ownerUserId = ownerUserId;
        this.cartType = cartType;
        this.campaignId = campaignId;
        this.roleSegment = roleSegment;
        this.partnerContextId = partnerContextId;
        this.status = CartStatus.ACTIVE;
    }

    public UUID cartId() {
        return cartId;
    }

    public String ownerUserId() {
        return ownerUserId;
    }

    public CartType cartType() {
        return cartType;
    }

    public String campaignId() {
        return campaignId;
    }

    public RoleSegment roleSegment() {
        return roleSegment;
    }

    public String partnerContextId() {
        return partnerContextId;
    }

    public CartStatus status() {
        return status;
    }

    public void status(CartStatus status) {
        this.status = status;
    }

    public int version() {
        return version;
    }

    public void incrementVersion() {
        this.version++;
    }

    public List<CartLineResponse> lines() {
        return lines;
    }

    public List<AppliedOfferResponse> appliedOffers() {
        return appliedOffers;
    }

    public CartTotalsResponse totals() {
        return totals;
    }

    public void totals(CartTotalsResponse totals) {
        this.totals = totals;
    }
}
