package com.bestorigin.monolith.employee.impl.service;

import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeConfirmOrderRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeEscalationPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderCreateRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOperatorOrderResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryDetailsResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryFilterRequest;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderHistoryPageResponse;
import com.bestorigin.monolith.employee.api.EmployeeDtos.EmployeeOrderSupportResponse;
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
}
