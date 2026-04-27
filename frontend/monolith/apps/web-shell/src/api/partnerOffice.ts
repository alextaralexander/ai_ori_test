export interface PartnerOfficeOrderSummaryResponse {
  orderNumber: string;
  officeId: string;
  regionId: string;
  partnerPersonNumber: string;
  customerId: string;
  campaignId: string;
  supplyId: string;
  pickupPointId: string;
  orderStatus: string;
  paymentStatus: string;
  assemblyStatus: string;
  deliveryStatus: string;
  hasDeviation: boolean;
  grandTotalAmount: string;
  currency: string;
}

export interface PartnerOfficeOrderPageResponse {
  items: PartnerOfficeOrderSummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface PartnerOfficeSupplySummaryResponse {
  supplyId: string;
  officeId: string;
  regionId: string;
  warehouseId: string;
  externalWmsDocumentId: string;
  status: string;
  plannedShipmentAt: string;
  plannedArrivalAt: string;
  actualArrivalAt: string;
  orderCount: number;
  boxCount: number;
  skuCount: number;
  deviationCount: number;
}

export interface PartnerOfficeSupplyItemResponse {
  sku: string;
  productName: string;
  expectedQuantity: number;
  acceptedQuantity: number;
  boxNumber: string;
}

export interface PartnerOfficeMovementResponse {
  movementType: string;
  sourceSystem: string;
  externalReference: string;
  occurredAt: string;
  actorId: string;
}

export interface PartnerOfficeDeviationResponse {
  deviationId: string;
  deviationType: string;
  sku: string;
  quantity: number;
  reasonCode: string;
  comment: string;
  claimId: string;
}

export interface PartnerOfficeSupplyDetailsResponse {
  supply: PartnerOfficeSupplySummaryResponse;
  orders: PartnerOfficeOrderSummaryResponse[];
  items: PartnerOfficeSupplyItemResponse[];
  movements: PartnerOfficeMovementResponse[];
  deviations: PartnerOfficeDeviationResponse[];
  availableActions: string[];
}

export interface PartnerOfficeSupplyOrderDetailsResponse {
  order: PartnerOfficeOrderSummaryResponse;
  items: PartnerOfficeSupplyItemResponse[];
  movements: PartnerOfficeMovementResponse[];
  deviations: PartnerOfficeDeviationResponse[];
  workflowLinks: Record<string, string>;
}

export interface PartnerOfficeSupplyPageResponse {
  items: PartnerOfficeSupplySummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface PartnerOfficeActionResponse {
  messageCode: string;
  correlationId: string;
}

export interface PartnerOfficeReportResponse {
  officeId: string;
  regionId: string;
  supplyCount: number;
  orderCount: number;
  shortageCount: number;
  damagedCount: number;
  shipmentSlaPercent: string;
  acceptanceSlaPercent: string;
  escalations: Array<{
    supplyId: string;
    reasonCode: string;
    ownerUserId: string;
    dueAt: string;
    status: string;
  }>;
}

interface ErrorResponse {
  code: string;
}

export class PartnerOfficeApiError extends Error {
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
    'Idempotency-Key': idempotencyKey ?? `partner-office-${Date.now()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as ErrorResponse;
    throw new PartnerOfficeApiError(response.status, error.code);
  }
  return body as T;
}

export async function searchPartnerOfficeOrders(params: URLSearchParams): Promise<PartnerOfficeOrderPageResponse> {
  const query = params.toString();
  const response = await fetch(`/api/partner-office/orders${query ? `?${query}` : ''}`, { headers: authHeaders() });
  return readJson<PartnerOfficeOrderPageResponse>(response);
}

export async function searchPartnerOfficeSupply(params: URLSearchParams): Promise<PartnerOfficeSupplyPageResponse> {
  const query = params.toString();
  const response = await fetch(`/api/partner-office/supply${query ? `?${query}` : ''}`, { headers: authHeaders() });
  return readJson<PartnerOfficeSupplyPageResponse>(response);
}

export async function getPartnerOfficeSupply(supplyId: string): Promise<PartnerOfficeSupplyDetailsResponse> {
  const response = await fetch(`/api/partner-office/supply/${encodeURIComponent(supplyId)}`, { headers: authHeaders() });
  return readJson<PartnerOfficeSupplyDetailsResponse>(response);
}

export async function getPartnerOfficeSupplyOrder(orderNumber: string): Promise<PartnerOfficeSupplyOrderDetailsResponse> {
  const response = await fetch(`/api/partner-office/supply/orders/${encodeURIComponent(orderNumber)}`, { headers: authHeaders() });
  return readJson<PartnerOfficeSupplyOrderDetailsResponse>(response);
}

export async function transitionPartnerOfficeSupply(supplyId: string, targetStatus: string): Promise<PartnerOfficeActionResponse> {
  const response = await fetch(`/api/partner-office/supply/${encodeURIComponent(supplyId)}/transition`, {
    method: 'POST',
    headers: jsonHeaders(`partner-office-transition-${supplyId}-${targetStatus}`),
    body: JSON.stringify({ targetStatus, reasonCode: 'UI_ACTION' }),
  });
  return readJson<PartnerOfficeActionResponse>(response);
}

export async function recordPartnerOfficeDeviation(orderNumber: string, supplyId: string): Promise<PartnerOfficeActionResponse> {
  const response = await fetch(`/api/partner-office/supply/orders/${encodeURIComponent(orderNumber)}/deviations`, {
    method: 'POST',
    headers: jsonHeaders(`partner-office-deviation-${orderNumber}`),
    body: JSON.stringify({ supplyId, deviationType: 'SHORTAGE', sku: 'SKU-018-LIP-001', quantity: 1, reasonCode: 'SHORT_PACKED' }),
  });
  return readJson<PartnerOfficeActionResponse>(response);
}

export async function getPartnerOfficeReport(params: URLSearchParams): Promise<PartnerOfficeReportResponse> {
  const query = params.toString();
  const response = await fetch(`/api/partner-office/report${query ? `?${query}` : ''}`, { headers: authHeaders() });
  return readJson<PartnerOfficeReportResponse>(response);
}
