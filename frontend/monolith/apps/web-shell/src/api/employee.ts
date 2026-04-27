export interface EmployeeCustomerResponse {
  customerId: string;
  partnerPersonNumber: string;
  displayName: string;
  segment: string;
  maskedPhone: string;
  maskedEmail: string;
}

export interface EmployeeOrderItemResponse {
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: string;
  totalPrice: string;
  availabilityStatus: string;
}

export interface EmployeeCartResponse {
  cartId: string;
  cartType: 'MAIN' | 'SUPPLEMENTARY';
  items: EmployeeOrderItemResponse[];
  subtotalAmount: string;
  currencyCode: string;
}

export interface EmployeeOrderSummaryResponse {
  orderNumber: string;
  createdAt: string;
  orderStatus: string;
  paymentStatus: string;
  deliveryStatus: string;
  grandTotalAmount: string;
  currencyCode: string;
}

export interface EmployeeWarningResponse {
  code: string;
  severity: string;
  target: string;
}

export interface EmployeeWorkspaceResponse {
  sessionId: string;
  customer: EmployeeCustomerResponse;
  activeCart: EmployeeCartResponse;
  recentOrders: EmployeeOrderSummaryResponse[];
  warnings: EmployeeWarningResponse[];
  auditContext: {
    actorUserId: string;
    supportReasonCode: string;
    sourceChannel: string;
    auditRecorded: boolean;
  };
  linkedRoutes: Record<string, string>;
}

export interface EmployeeOperatorOrderResponse {
  operatorOrderId: string;
  checkoutId: string;
  orderNumber: string;
  paymentStatus: string;
  deliveryStatus: string;
  grandTotalAmount: string;
  currencyCode: string;
  nextAction: string;
  messageCode: string;
  auditRecorded: boolean;
}

export interface EmployeeOrderSupportResponse {
  orderNumber: string;
  customer: EmployeeCustomerResponse;
  order: EmployeeOrderSummaryResponse;
  timeline: Array<{
    eventType: string;
    publicStatus: string;
    sourceSystem: string;
    descriptionCode: string;
    occurredAt: string;
  }>;
  supportActions: EmployeeSupportActionResponse[];
  warnings: EmployeeWarningResponse[];
  linkedRoutes: Record<string, string>;
}

export interface EmployeeSupportActionResponse {
  actionId: string;
  orderNumber: string;
  actionType: string;
  reasonCode: string;
  amount: string;
  supervisorRequired: boolean;
  visibility: string;
  messageCode: string;
  createdAt: string;
}

export interface EmployeeEscalationPageResponse {
  items: EmployeeSupportActionResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface EmployeeOrderHistorySummaryResponse {
  orderId: string;
  orderNumber: string;
  campaignCode: string;
  customerId: string;
  partnerId: string;
  customerDisplayName: string;
  partnerDisplayName: string;
  maskedPhone: string;
  maskedEmail: string;
  orderStatus: string;
  paymentStatus: string;
  deliveryStatus: string;
  fulfillmentStatus: string;
  totalAmount: string;
  currencyCode: string;
  problemFlags: string[];
  linkedRoutes: Record<string, string>;
  updatedAt: string;
}

export interface EmployeeOrderHistoryPageResponse {
  items: EmployeeOrderHistorySummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
  auditRecorded: boolean;
  availableProblemFilters: string[];
}

export interface EmployeeOrderHistoryDetailsResponse extends EmployeeOrderHistorySummaryResponse {
  items: Array<{
    sku: string;
    productName: string;
    quantity: number;
    unitPrice: string;
    totalPrice: string;
    promoCode: string;
    bonusPoints: number;
    reserveStatus: string;
  }>;
  paymentEvents: EmployeeLinkedEventResponse[];
  deliveryEvents: EmployeeLinkedEventResponse[];
  wmsEvents: EmployeeLinkedEventResponse[];
  supportCaseIds: string[];
  claimIds: string[];
  paymentEventIds: string[];
  wmsBatchId: string;
  deliveryTrackingId: string;
  manualAdjustmentPresent: boolean;
  supervisorRequired: boolean;
  sourceChannel: string;
  auditEvents: Array<{
    eventType: string;
    actorUserId: string;
    actorRole?: string;
    targetEntityType: string;
    targetEntityId: string;
    occurredAt: string;
  }>;
}

export interface EmployeeLinkedEventResponse {
  eventId: string;
  eventType: string;
  status: string;
  sourceSystem: string;
  occurredAt: string;
  messageCode: string;
}

export interface EmployeeClaimItemResponse {
  sku: string;
  productCode: string;
  productName: string;
  quantity: number;
  problemType: string;
  requestedResolution: string;
  approvedResolution: string;
  compensationAmount: string;
}

export interface EmployeeClaimRouteTaskResponse {
  taskId: string;
  taskType: string;
  status: string;
  assigneeRole: string;
  assigneeId?: string;
  dueAt: string;
  completedAt?: string;
  resultCode: string;
}

export interface EmployeeClaimDetailsResponse {
  claimId: string;
  claimNumber: string;
  orderNumber: string;
  customerId: string;
  partnerId: string;
  status: string;
  slaState: string;
  slaDueAt: string;
  requestedResolution: string;
  approvedResolution: string;
  compensationAmount: string;
  currencyCode: string;
  publicReasonMnemonic: string;
  supervisorRequired: boolean;
  items: EmployeeClaimItemResponse[];
  attachments: Array<{ attachmentId: string; filename: string; mimeType: string; sizeBytes: number; accessPolicy: string }>;
  routeTasks: EmployeeClaimRouteTaskResponse[];
  auditEvents: Array<{ auditEventId: string; actorUserId: string; actorRole: string; actionType: string; supportReasonCode: string; sourceRoute: string; occurredAt: string }>;
  availableActions: string[];
}

export interface EmployeeClaimSummaryResponse {
  claimId: string;
  claimNumber: string;
  orderNumber: string;
  customerOrPartnerLabel: string;
  maskedContact: string;
  status: string;
  slaState: string;
  slaDueAt: string;
  resolutionType: string;
  compensationAmount: string;
  currencyCode: string;
  assignee: string;
  responsibleRole: string;
  updatedAt: string;
  availableActions: string[];
}

export interface EmployeeClaimPageResponse {
  items: EmployeeClaimSummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
  auditRecorded: boolean;
  availableFilters: string[];
}

interface ErrorResponse {
  code: string;
}

export class EmployeeApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(status: number, code: string) {
    super(code);
    this.code = code;
    this.status = status;
  }
}

function authHeaders(): HeadersInit {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  return {
    Accept: 'application/json',
    'Accept-Language': 'ru-RU',
    Authorization: `Bearer ${role}-api-session-ui`,
  };
}

function jsonHeaders(idempotencyKey?: string): HeadersInit {
  return {
    ...authHeaders(),
    'Content-Type': 'application/json',
    'Idempotency-Key': idempotencyKey ?? `employee-${Date.now()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as ErrorResponse;
    throw new EmployeeApiError(response.status, error.code);
  }
  return body as T;
}

export async function getEmployeeWorkspace(query: string): Promise<EmployeeWorkspaceResponse> {
  const response = await fetch(`/api/employee/workspace?query=${encodeURIComponent(query)}`, { headers: authHeaders() });
  return readJson<EmployeeWorkspaceResponse>(response);
}

export async function createEmployeeOperatorOrder(customerId: string): Promise<EmployeeOperatorOrderResponse> {
  const response = await fetch('/api/employee/operator-orders', {
    method: 'POST',
    headers: jsonHeaders(`employee-new-order-${customerId}`),
    body: JSON.stringify({
      targetCustomerId: customerId,
      supportReasonCode: 'PHONE_ORDER',
      cartType: 'MAIN',
      items: [{ sku: 'SKU-019-CREAM-001', quantity: 2 }],
    }),
  });
  return readJson<EmployeeOperatorOrderResponse>(response);
}

export async function getEmployeeOrderSupport(orderNumber: string): Promise<EmployeeOrderSupportResponse> {
  const response = await fetch(`/api/employee/order-support/${encodeURIComponent(orderNumber)}`, { headers: authHeaders() });
  return readJson<EmployeeOrderSupportResponse>(response);
}

export async function addEmployeeNote(orderNumber: string): Promise<EmployeeSupportActionResponse> {
  const response = await fetch(`/api/employee/order-support/${encodeURIComponent(orderNumber)}/notes`, {
    method: 'POST',
    headers: jsonHeaders(`employee-note-${orderNumber}`),
    body: JSON.stringify({ reasonCode: 'DELIVERY_DELAY', note: 'internal-support-note' }),
  });
  return readJson<EmployeeSupportActionResponse>(response);
}

export async function recordEmployeeAdjustment(orderNumber: string): Promise<EmployeeSupportActionResponse> {
  const response = await fetch(`/api/employee/order-support/${encodeURIComponent(orderNumber)}/adjustments`, {
    method: 'POST',
    headers: jsonHeaders(`employee-adjustment-${orderNumber}`),
    body: JSON.stringify({ reasonCode: 'DELIVERY_DELAY', amount: 350 }),
  });
  return readJson<EmployeeSupportActionResponse>(response);
}

export async function getEmployeeEscalations(): Promise<EmployeeEscalationPageResponse> {
  const response = await fetch('/api/employee/supervisor/escalations', { headers: authHeaders() });
  return readJson<EmployeeEscalationPageResponse>(response);
}

export async function getEmployeeOrderHistory(params: URLSearchParams): Promise<EmployeeOrderHistoryPageResponse> {
  const query = new URLSearchParams(params);
  const response = await fetch(`/api/employee/order-history?${query.toString()}`, { headers: authHeaders() });
  return readJson<EmployeeOrderHistoryPageResponse>(response);
}

export async function getEmployeeOrderHistoryDetails(orderId: string): Promise<EmployeeOrderHistoryDetailsResponse> {
  const response = await fetch(`/api/employee/order-history/${encodeURIComponent(orderId)}`, { headers: authHeaders() });
  return readJson<EmployeeOrderHistoryDetailsResponse>(response);
}

export async function submitEmployeeClaim(orderNumber: string, supportReasonCode: string): Promise<EmployeeClaimDetailsResponse> {
  const response = await fetch('/api/employee/submit-claim', {
    method: 'POST',
    headers: jsonHeaders(`employee-claim-${orderNumber}`),
    body: JSON.stringify({
      customerId: 'CUST-021-001',
      partnerId: 'PART-021-001',
      orderNumber,
      sourceChannel: 'PHONE',
      supportReasonCode,
      requestedResolution: 'REFUND',
      comment: 'STR_MNEMO_EMPLOYEE_CLAIM_CREATED',
      items: [{ sku: 'SKU-021-001', productCode: 'PRD-021-001', quantity: 1, problemType: 'DAMAGED_ITEM', requestedResolution: 'REFUND' }],
      attachments: [{ fileId: 'ATT-021-001', filename: 'claim-photo.jpg', mimeType: 'image/jpeg', sizeBytes: 512000, accessPolicy: 'INTERNAL' }],
    }),
  });
  return readJson<EmployeeClaimDetailsResponse>(response);
}

export async function getEmployeeClaims(params: URLSearchParams): Promise<EmployeeClaimPageResponse> {
  const query = new URLSearchParams(params);
  const response = await fetch(`/api/employee/claims?${query.toString()}`, { headers: authHeaders() });
  return readJson<EmployeeClaimPageResponse>(response);
}

export async function getEmployeeClaimDetails(claimId: string): Promise<EmployeeClaimDetailsResponse> {
  const response = await fetch(`/api/employee/claims/${encodeURIComponent(claimId)}?supportReasonCode=EMPLOYEE_CLAIM_VIEW`, { headers: authHeaders() });
  return readJson<EmployeeClaimDetailsResponse>(response);
}

export async function transitionEmployeeClaim(claimId: string, transitionCode: string): Promise<EmployeeClaimDetailsResponse> {
  const response = await fetch(`/api/employee/claims/${encodeURIComponent(claimId)}/transitions`, {
    method: 'POST',
    headers: jsonHeaders(`employee-claim-transition-${claimId}-${transitionCode}`),
    body: JSON.stringify({ transitionCode, supportReasonCode: transitionCode === 'APPROVE_COMPENSATION' ? 'SUPERVISOR_REVIEW' : 'CUSTOMER_CALL', approvedCompensationAmount: transitionCode === 'APPROVE_COMPENSATION' ? 2500 : 1250 }),
  });
  return readJson<EmployeeClaimDetailsResponse>(response);
}
