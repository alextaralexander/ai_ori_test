export type CheckoutType = 'MAIN' | 'SUPPLEMENTARY';
export type CheckoutStatus = 'DRAFT' | 'VALIDATION_REQUIRED' | 'READY_TO_CONFIRM' | 'CONFIRMED' | 'BLOCKED' | 'EXPIRED';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'EXPIRED' | 'CANCELLED';
export type NextAction = 'PAYMENT_REDIRECT' | 'WAIT_PAYMENT' | 'ORDER_DETAILS' | 'FIX_CHECKOUT';
export type ClaimStatus = 'DRAFT' | 'SUBMITTED' | 'IN_REVIEW' | 'WAREHOUSE_CHECK' | 'APPROVED' | 'PARTIALLY_APPROVED' | 'REJECTED' | 'CLOSED';
export type ClaimResolution = 'REFUND' | 'REPLACEMENT' | 'MISSING_ITEM' | 'SERVICE_REVIEW' | 'REJECTED';
export type PartnerOfflineOrderActionType = 'REPEAT_ORDER' | 'SERVICE_ADJUSTMENT';

export interface StartCheckoutRequest {
  cartId: string;
  checkoutType: CheckoutType;
  vipMode?: boolean;
  superOrderMode?: boolean;
}

export interface RecipientRequest {
  recipientType: string;
  fullName: string;
  phone: string;
  email?: string;
}

export interface AddressRequest {
  deliveryTargetType: string;
  addressId?: string | null;
  pickupPointId?: string | null;
  country?: string | null;
  region?: string | null;
  city?: string | null;
  street?: string | null;
  house?: string | null;
  apartment?: string | null;
  postalCode?: string | null;
}

export interface DeliveryOptionResponse {
  code: string;
  name: string;
  available: boolean;
  price: number;
  estimatedInterval: string;
  reasonMnemo?: string | null;
}

export interface PaymentResponse {
  paymentMethodCode: string;
  paymentSessionId: string;
  paymentStatus: PaymentStatus;
  amountToPay: number;
}

export interface BenefitResponse {
  benefitType: string;
  benefitCode: string;
  appliedAmount: number;
  status: string;
  reasonMnemo?: string | null;
}

export interface CheckoutItemResponse {
  productCode: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  availabilityStatus: string;
  reserveStatus: string;
  blockingReasonMnemo?: string | null;
}

export interface CheckoutTotalsResponse {
  subtotalAmount: number;
  deliveryAmount: number;
  discountAmount: number;
  walletAmount: number;
  cashbackAmount: number;
  grandTotalAmount: number;
}

export interface ValidationReasonResponse {
  code: string;
  severity: string;
  target: string;
}

export interface CheckoutValidationResponse {
  valid: boolean;
  reasons: ValidationReasonResponse[];
}

export interface CheckoutDraftResponse {
  id: string;
  checkoutType: CheckoutType;
  cartId: string;
  campaignId: string;
  status: CheckoutStatus;
  version: number;
  recipient?: RecipientRequest | null;
  address?: AddressRequest | null;
  deliveryOptions: DeliveryOptionResponse[];
  selectedDelivery?: DeliveryOptionResponse | null;
  selectedPayment?: PaymentResponse | null;
  benefits: BenefitResponse[];
  items: CheckoutItemResponse[];
  totals: CheckoutTotalsResponse;
  validation: CheckoutValidationResponse;
}

export interface OrderConfirmationResponse {
  orderNumber: string;
  orderType: CheckoutType;
  status: string;
  paymentStatus: PaymentStatus;
  deliveryStatus: string;
  paymentSessionId: string;
  totals: CheckoutTotalsResponse;
  nextAction: NextAction;
  reasons: ValidationReasonResponse[];
}

export interface OrderHistoryLineResponse {
  productCode: string;
  sku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  discountAmount: number;
  totalPrice: number;
  gift: boolean;
  repeatAvailable: boolean;
  claimAvailable: boolean;
  limitationReasonMnemo?: string | null;
}

export interface OrderHistoryItemResponse {
  orderNumber: string;
  orderType: CheckoutType;
  campaignId: string;
  createdAt: string;
  orderStatus: string;
  paymentStatus: PaymentStatus;
  deliveryStatus: string;
  grandTotalAmount: number;
  currencyCode: string;
  summaryItems: OrderHistoryLineResponse[];
  warnings: ValidationReasonResponse[];
}

export interface OrderHistoryPageResponse {
  items: OrderHistoryItemResponse[];
  page: number;
  size: number;
  totalElements: number;
  hasNext: boolean;
}

export interface OrderDetailsResponse extends OrderHistoryItemResponse {
  items: OrderHistoryLineResponse[];
  totals: CheckoutTotalsResponse;
  delivery: {
    deliveryTargetType: string;
    maskedRecipientName: string;
    maskedPhone: string;
    city: string;
    addressLine: string;
    expectedInterval: string;
    trackingNumber: string;
  };
  payment: {
    paymentMethodCode: string;
    paymentStatus: PaymentStatus;
    amountToPay: number;
    paidAmount: number;
    paymentActionAvailable: boolean;
  };
  events: Array<{
    eventType: string;
    publicStatus: string;
    sourceSystem: string;
    descriptionMnemo?: string | null;
    occurredAt: string;
  }>;
  actions: {
    paymentAvailable: boolean;
    repeatOrderAvailable: boolean;
    claimAvailable: boolean;
  };
  auditRecorded?: boolean;
  businessVolume?: number | null;
}

export interface RepeatOrderResponse {
  status: 'COMPLETED' | 'PARTIAL' | 'REJECTED';
  targetCartType: CheckoutType;
  addedItems: OrderHistoryLineResponse[];
  rejectedItems: OrderHistoryLineResponse[];
  reasonMnemo?: string | null;
}

export interface OrderClaimItemRequest {
  sku: string;
  quantity: number;
}

export interface OrderClaimCreateRequest {
  orderNumber: string;
  reasonCode: string;
  requestedResolution: ClaimResolution;
  comment?: string;
  items: OrderClaimItemRequest[];
}

export interface OrderClaimSummaryResponse {
  claimId: string;
  orderNumber: string;
  status: ClaimStatus;
  requestedResolution: ClaimResolution;
  refundAmount: number;
  currencyCode: string;
  updatedAt: string;
  partnerImpact: boolean;
}

export interface OrderClaimPageResponse {
  items: OrderClaimSummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
  hasNext: boolean;
}

export interface OrderClaimDetailsResponse extends OrderClaimSummaryResponse {
  approvedResolution: ClaimResolution;
  publicReasonMnemo?: string | null;
  auditRecorded?: boolean;
  businessVolumeDelta?: number | null;
  items: Array<{
    productCode: string;
    sku: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    refundAmount: number;
    requestedResolution: ClaimResolution;
    approvedResolution: ClaimResolution;
    status: string;
    reasonMnemo?: string | null;
  }>;
  events: Array<{
    eventType: string;
    publicStatus: string;
    sourceSystem: string;
    descriptionMnemo?: string | null;
    occurredAt: string;
  }>;
  comments: Array<{
    authorRole: string;
    visibility: string;
    messageMnemo?: string | null;
    createdAt: string;
  }>;
  attachments: Array<{
    attachmentId: string;
    fileName: string;
    contentType: string;
    sizeBytes: number;
  }>;
  nextAction: string;
}

export interface PartnerOfflineOrderSummaryResponse {
  orderNumber: string;
  campaignId: string;
  createdAt: string;
  customerId: string;
  customerName: string;
  customerSegment: string;
  partnerPersonNumber: string;
  partnerDisplayName: string;
  orderStatus: string;
  paymentStatus: PaymentStatus;
  deliveryStatus: string;
  bonusAccrualStatus: string;
  businessVolume: number;
  grandTotalAmount: number;
  currencyCode: string;
  warnings: ValidationReasonResponse[];
}

export interface PartnerOfflineOrderPageResponse {
  items: PartnerOfflineOrderSummaryResponse[];
  page: number;
  size: number;
  totalElements: number;
  hasNext: boolean;
  messageMnemo: string;
}

export interface PartnerOfflineOrderDetailsResponse extends PartnerOfflineOrderSummaryResponse {
  items: OrderHistoryLineResponse[];
  delivery: OrderDetailsResponse['delivery'];
  payment: OrderDetailsResponse['payment'];
  events: OrderDetailsResponse['events'];
  availableActions: string[];
  linkedEntities: Record<string, string>;
}

export interface PartnerOfflineOrderActionResponse {
  orderNumber: string;
  actionType: PartnerOfflineOrderActionType;
  resultMnemo: string;
  events: OrderDetailsResponse['events'];
}

interface ErrorResponse {
  code: string;
  details?: ValidationReasonResponse[];
}

export class OrderApiError extends Error {
  readonly code: string;
  readonly details: ValidationReasonResponse[];
  readonly status: number;

  constructor(status: number, code: string, details: ValidationReasonResponse[] = []) {
    super(code);
    this.code = code;
    this.details = details;
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
    'Idempotency-Key': idempotencyKey ?? `ui-order-${Date.now()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as ErrorResponse;
    throw new OrderApiError(response.status, error.code, error.details ?? []);
  }
  return body as T;
}

export async function startCheckout(request: StartCheckoutRequest): Promise<CheckoutDraftResponse> {
  const response = await fetch('/api/order/checkouts', {
    method: 'POST',
    headers: jsonHeaders(`start-${request.cartId}-${request.checkoutType}`),
    body: JSON.stringify(request),
  });
  return readJson<CheckoutDraftResponse>(response);
}

export async function updateRecipient(checkoutId: string, request: RecipientRequest): Promise<CheckoutDraftResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/recipient`, {
    method: 'PUT',
    headers: jsonHeaders(`recipient-${checkoutId}`),
    body: JSON.stringify(request),
  });
  return readJson<CheckoutDraftResponse>(response);
}

export async function updateAddress(checkoutId: string, request: AddressRequest): Promise<CheckoutDraftResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/address`, {
    method: 'PUT',
    headers: jsonHeaders(`address-${checkoutId}`),
    body: JSON.stringify(request),
  });
  return readJson<CheckoutDraftResponse>(response);
}

export async function selectDelivery(checkoutId: string, deliveryMethodCode: string): Promise<CheckoutDraftResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/delivery`, {
    method: 'PUT',
    headers: jsonHeaders(`delivery-${checkoutId}`),
    body: JSON.stringify({ deliveryMethodCode }),
  });
  return readJson<CheckoutDraftResponse>(response);
}

export async function applyBenefits(checkoutId: string, walletAmount: number): Promise<CheckoutDraftResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/benefits`, {
    method: 'PUT',
    headers: jsonHeaders(`benefits-${checkoutId}`),
    body: JSON.stringify({ walletAmount, cashbackAmount: 0, benefitCodes: [] }),
  });
  return readJson<CheckoutDraftResponse>(response);
}

export async function selectPayment(checkoutId: string, paymentMethodCode: string): Promise<CheckoutDraftResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/payment`, {
    method: 'PUT',
    headers: jsonHeaders(`payment-${checkoutId}`),
    body: JSON.stringify({ paymentMethodCode }),
  });
  return readJson<CheckoutDraftResponse>(response);
}

export async function validateCheckout(checkoutId: string): Promise<CheckoutValidationResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/validation`, {
    method: 'POST',
    headers: jsonHeaders(`validation-${checkoutId}`),
    body: '{}',
  });
  return readJson<CheckoutValidationResponse>(response);
}

export async function confirmCheckout(checkoutId: string, checkoutVersion: number): Promise<OrderConfirmationResponse> {
  const response = await fetch(`/api/order/checkouts/${checkoutId}/confirm`, {
    method: 'POST',
    headers: jsonHeaders(`confirm-${checkoutId}`),
    body: JSON.stringify({ checkoutVersion }),
  });
  return readJson<OrderConfirmationResponse>(response);
}

export async function searchOrderHistory(params: URLSearchParams): Promise<OrderHistoryPageResponse> {
  const query = params.toString();
  const response = await fetch(`/api/order/order-history${query ? `?${query}` : ''}`, {
    headers: authHeaders(),
  });
  return readJson<OrderHistoryPageResponse>(response);
}

export async function getOrderDetails(orderNumber: string): Promise<OrderDetailsResponse> {
  const response = await fetch(`/api/order/order-history/${encodeURIComponent(orderNumber)}`, {
    headers: authHeaders(),
  });
  return readJson<OrderDetailsResponse>(response);
}

export async function repeatOrder(orderNumber: string): Promise<RepeatOrderResponse> {
  const response = await fetch(`/api/order/order-history/${encodeURIComponent(orderNumber)}/repeat`, {
    method: 'POST',
    headers: jsonHeaders(`repeat-${orderNumber}`),
    body: '{}',
  });
  return readJson<RepeatOrderResponse>(response);
}

export async function createOrderClaim(request: OrderClaimCreateRequest): Promise<OrderClaimDetailsResponse> {
  const response = await fetch('/api/order/claims', {
    method: 'POST',
    headers: jsonHeaders(`claim-${request.orderNumber}-${request.items.map((item) => item.sku).join('-')}`),
    body: JSON.stringify(request),
  });
  return readJson<OrderClaimDetailsResponse>(response);
}

export async function searchOrderClaims(params: URLSearchParams): Promise<OrderClaimPageResponse> {
  const query = params.toString();
  const response = await fetch(`/api/order/claims${query ? `?${query}` : ''}`, {
    headers: authHeaders(),
  });
  return readJson<OrderClaimPageResponse>(response);
}

export async function getOrderClaimDetails(claimId: string): Promise<OrderClaimDetailsResponse> {
  const response = await fetch(`/api/order/claims/${encodeURIComponent(claimId)}`, {
    headers: authHeaders(),
  });
  return readJson<OrderClaimDetailsResponse>(response);
}

export async function searchPartnerOfflineOrders(params: URLSearchParams): Promise<PartnerOfflineOrderPageResponse> {
  const query = params.toString();
  const response = await fetch(`/api/order/partner-offline-orders${query ? `?${query}` : ''}`, {
    headers: authHeaders(),
  });
  return readJson<PartnerOfflineOrderPageResponse>(response);
}

export async function getPartnerOfflineOrder(orderNumber: string): Promise<PartnerOfflineOrderDetailsResponse> {
  const response = await fetch(`/api/order/partner-offline-orders/${encodeURIComponent(orderNumber)}`, {
    headers: authHeaders(),
  });
  return readJson<PartnerOfflineOrderDetailsResponse>(response);
}

export async function executePartnerOfflineOrderAction(orderNumber: string, actionType: PartnerOfflineOrderActionType): Promise<PartnerOfflineOrderActionResponse> {
  const response = await fetch(`/api/order/partner-offline-orders/${encodeURIComponent(orderNumber)}/actions`, {
    method: 'POST',
    headers: jsonHeaders(`partner-offline-${orderNumber}-${actionType}`),
    body: JSON.stringify({ actionType }),
  });
  return readJson<PartnerOfflineOrderActionResponse>(response);
}
