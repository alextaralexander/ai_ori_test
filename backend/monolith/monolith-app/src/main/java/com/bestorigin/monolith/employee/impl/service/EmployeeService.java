package com.bestorigin.monolith.employee.impl.service;

import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeConfirmOrderRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAddressResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAddressUpsertRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeAddressesResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeClaimTransitionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeContactResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeContactUpsertRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeContactsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeDocumentCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeDocumentResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeDocumentsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedDecisionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedRequestCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedRequestResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeElevatedSessionResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeEscalationPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryFilterRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderSupportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerCardResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeePartnerOrderReportResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeProfileGeneralResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeProfileGeneralUpdateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeProfileSettingsSummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSecuritySummaryResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSuperUserDashboardResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSupportActionRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeSupportActionResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeWorkspaceResponse;
import java.util.UUID;

public interface EmployeeService {

    EmployeeWorkspaceResponse workspace(String userContext, String query);

    EmployeeOperatorOrderResponse createOperatorOrder(String userContext, EmployeeOperatorOrderCreateRequest request, String idempotencyKey);

    EmployeeOperatorOrderResponse confirmOperatorOrder(String userContext, UUID operatorOrderId, EmployeeConfirmOrderRequest request, String idempotencyKey);

    EmployeeOrderSupportResponse orderSupport(String userContext, String orderNumber);

    EmployeeSupportActionResponse addInternalNote(String userContext, String orderNumber, EmployeeSupportActionRequest request, String idempotencyKey);

    EmployeeSupportActionResponse recordAdjustment(String userContext, String orderNumber, EmployeeSupportActionRequest request, String idempotencyKey);

    EmployeeSupportActionResponse createEscalation(String userContext, String orderNumber, EmployeeSupportActionRequest request, String idempotencyKey);

    EmployeeEscalationPageResponse supervisorEscalations(String userContext, int page, int size);

    EmployeeOrderHistoryPageResponse orderHistory(String userContext, EmployeeOrderHistoryFilterRequest request);

    EmployeeOrderHistoryDetailsResponse orderHistoryDetails(String userContext, String orderId);

    EmployeeClaimDetailsResponse submitClaim(String userContext, EmployeeClaimCreateRequest request, String idempotencyKey);

    EmployeeClaimPageResponse claims(String userContext, String claimStatus, String dateFrom, String dateTo, String slaState, String responsibleRole, String assigneeId, String resolutionType, String sourceChannel, String warehouseCode, String financeStatus, String query, int page, int size, String sort);

    EmployeeClaimDetailsResponse claimDetails(String userContext, String claimId, String supportReasonCode);

    EmployeeClaimDetailsResponse transitionClaim(String userContext, String claimId, EmployeeClaimTransitionRequest request, String idempotencyKey);

    EmployeePartnerCardResponse partnerCard(String userContext, String query, String supportReasonCode, String regionCode);

    EmployeePartnerCardResponse partnerCardById(String userContext, String partnerId, String supportReasonCode);

    EmployeePartnerOrderReportResponse partnerOrderReport(String userContext, String partnerId, String personNumber, String dateFrom, String dateTo, String campaignCode, String orderStatus, String paymentStatus, String deliveryStatus, boolean problemOnly, String regionCode, int page, int size, String sort);

    EmployeeProfileSettingsSummaryResponse profileSettings(String userContext);

    EmployeeProfileGeneralResponse profileGeneral(String userContext);

    EmployeeProfileGeneralResponse updateProfileGeneral(String userContext, EmployeeProfileGeneralUpdateRequest request);

    EmployeeContactsResponse contacts(String userContext);

    EmployeeContactResponse createContact(String userContext, EmployeeContactUpsertRequest request);

    EmployeeAddressesResponse addresses(String userContext);

    EmployeeAddressResponse createAddress(String userContext, EmployeeAddressUpsertRequest request);

    EmployeeDocumentsResponse documents(String userContext);

    EmployeeDocumentResponse createDocument(String userContext, EmployeeDocumentCreateRequest request);

    EmployeeSecuritySummaryResponse security(String userContext);

    EmployeeSuperUserDashboardResponse superUser(String userContext);

    EmployeeElevatedRequestResponse createElevatedRequest(String userContext, EmployeeElevatedRequestCreateRequest request);

    EmployeeElevatedSessionResponse approveElevatedRequest(String userContext, UUID requestId, EmployeeElevatedDecisionRequest request);

    EmployeeElevatedRequestResponse rejectElevatedRequest(String userContext, UUID requestId, EmployeeElevatedDecisionRequest request);

    void closeElevatedSession(String userContext, UUID sessionId, EmployeeElevatedDecisionRequest request);
}
