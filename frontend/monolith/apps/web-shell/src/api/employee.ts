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
