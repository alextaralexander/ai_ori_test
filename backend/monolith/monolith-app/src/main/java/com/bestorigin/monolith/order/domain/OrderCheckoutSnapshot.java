package com.bestorigin.monolith.order.domain;

import com.bestorigin.monolith.order.api.OrderDtos.AddressRequest;
import com.bestorigin.monolith.order.api.OrderDtos.BenefitResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutItemResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutStatus;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutTotalsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutType;
import com.bestorigin.monolith.order.api.OrderDtos.DeliveryOptionResponse;
import com.bestorigin.monolith.order.api.OrderDtos.PaymentResponse;
import com.bestorigin.monolith.order.api.OrderDtos.RecipientRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderCheckoutSnapshot {

    private final UUID id;
    private final String ownerUserId;
    private final String cartId;
    private final CheckoutType checkoutType;
    private final String campaignId;
    private CheckoutStatus status;
    private long version;
    private RecipientRequest recipient;
    private AddressRequest address;
    private final List<DeliveryOptionResponse> deliveryOptions = new ArrayList<>();
    private DeliveryOptionResponse selectedDelivery;
    private PaymentResponse selectedPayment;
    private final List<BenefitResponse> benefits = new ArrayList<>();
    private final List<CheckoutItemResponse> items = new ArrayList<>();
    private CheckoutTotalsResponse totals;
    private String orderNumber;

    public OrderCheckoutSnapshot(UUID id, String ownerUserId, String cartId, CheckoutType checkoutType, String campaignId) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.cartId = cartId;
        this.checkoutType = checkoutType;
        this.campaignId = campaignId;
        this.status = CheckoutStatus.DRAFT;
    }

    public UUID id() {
        return id;
    }

    public String ownerUserId() {
        return ownerUserId;
    }

    public String cartId() {
        return cartId;
    }

    public CheckoutType checkoutType() {
        return checkoutType;
    }

    public String campaignId() {
        return campaignId;
    }

    public CheckoutStatus status() {
        return status;
    }

    public void status(CheckoutStatus status) {
        this.status = status;
    }

    public long version() {
        return version;
    }

    public void incrementVersion() {
        this.version++;
    }

    public RecipientRequest recipient() {
        return recipient;
    }

    public void recipient(RecipientRequest recipient) {
        this.recipient = recipient;
        incrementVersion();
    }

    public AddressRequest address() {
        return address;
    }

    public void address(AddressRequest address) {
        this.address = address;
        incrementVersion();
    }

    public List<DeliveryOptionResponse> deliveryOptions() {
        return deliveryOptions;
    }

    public DeliveryOptionResponse selectedDelivery() {
        return selectedDelivery;
    }

    public void selectedDelivery(DeliveryOptionResponse selectedDelivery) {
        this.selectedDelivery = selectedDelivery;
        incrementVersion();
    }

    public PaymentResponse selectedPayment() {
        return selectedPayment;
    }

    public void selectedPayment(PaymentResponse selectedPayment) {
        this.selectedPayment = selectedPayment;
        incrementVersion();
    }

    public List<BenefitResponse> benefits() {
        return benefits;
    }

    public List<CheckoutItemResponse> items() {
        return items;
    }

    public CheckoutTotalsResponse totals() {
        return totals;
    }

    public void totals(CheckoutTotalsResponse totals) {
        this.totals = totals;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public void orderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
