package com.bestorigin.monolith.order.impl.service;

import com.bestorigin.monolith.order.api.OrderDtos.AddressRequest;
import com.bestorigin.monolith.order.api.OrderDtos.BenefitApplyRequest;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutDraftResponse;
import com.bestorigin.monolith.order.api.OrderDtos.CheckoutValidationResponse;
import com.bestorigin.monolith.order.api.OrderDtos.ConfirmCheckoutRequest;
import com.bestorigin.monolith.order.api.OrderDtos.DeliverySelectionRequest;
import com.bestorigin.monolith.order.api.OrderDtos.OrderConfirmationResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderClaimCommentRequest;
import com.bestorigin.monolith.order.api.OrderDtos.OrderClaimCreateRequest;
import com.bestorigin.monolith.order.api.OrderDtos.OrderClaimDetailsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderClaimPageResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderDetailsResponse;
import com.bestorigin.monolith.order.api.OrderDtos.OrderHistoryPageResponse;
import com.bestorigin.monolith.order.api.OrderDtos.PaymentSelectionRequest;
import com.bestorigin.monolith.order.api.OrderDtos.RecipientRequest;
import com.bestorigin.monolith.order.api.OrderDtos.RepeatOrderResponse;
import com.bestorigin.monolith.order.api.OrderDtos.StartCheckoutRequest;
import java.util.UUID;

public interface OrderCheckoutService {

    CheckoutDraftResponse start(String userContextId, StartCheckoutRequest request, String idempotencyKey);

    CheckoutDraftResponse get(String userContextId, UUID checkoutId);

    CheckoutDraftResponse updateRecipient(String userContextId, UUID checkoutId, RecipientRequest request);

    CheckoutDraftResponse updateAddress(String userContextId, UUID checkoutId, AddressRequest request);

    CheckoutDraftResponse selectDelivery(String userContextId, UUID checkoutId, DeliverySelectionRequest request);

    CheckoutDraftResponse selectPayment(String userContextId, UUID checkoutId, PaymentSelectionRequest request, String idempotencyKey);

    CheckoutDraftResponse applyBenefits(String userContextId, UUID checkoutId, BenefitApplyRequest request, String idempotencyKey);

    CheckoutValidationResponse validate(String userContextId, UUID checkoutId);

    OrderConfirmationResponse confirm(String userContextId, UUID checkoutId, ConfirmCheckoutRequest request, String idempotencyKey);

    OrderConfirmationResponse getOrder(String userContextId, String orderNumber);

    OrderHistoryPageResponse searchOrderHistory(String userContextId, String query, String campaignId, String orderType, int page, int size);

    OrderDetailsResponse getOrderHistoryDetails(String userContextId, String orderNumber, String supportCustomerId, String reason);

    RepeatOrderResponse repeatOrder(String userContextId, String orderNumber, String idempotencyKey);

    OrderClaimPageResponse searchClaims(String userContextId, String query, String status, String resolution, int page, int size);

    OrderClaimDetailsResponse createClaim(String userContextId, OrderClaimCreateRequest request, String idempotencyKey);

    OrderClaimDetailsResponse getClaimDetails(String userContextId, String claimId, String supportCustomerId, String reason);

    OrderClaimDetailsResponse addClaimComment(String userContextId, String claimId, OrderClaimCommentRequest request, String idempotencyKey);
}
