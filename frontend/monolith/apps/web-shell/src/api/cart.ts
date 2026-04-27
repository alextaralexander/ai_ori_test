export type CartType = 'MAIN' | 'SUPPLEMENTARY';
export type CartStatus = 'ACTIVE' | 'BLOCKED' | 'READY_FOR_CHECKOUT' | 'ARCHIVED';
export type AvailabilityStatus = 'AVAILABLE' | 'LOW_STOCK' | 'RESERVED' | 'PARTIALLY_AVAILABLE' | 'UNAVAILABLE' | 'REMOVED_FROM_CAMPAIGN';

export interface AddCartItemRequest {
  productCode: string;
  quantity: number;
  source: 'SEARCH_RESULT' | 'PRODUCT_CARD' | 'SHOPPING_OFFER' | 'SUPPLEMENTARY_OFFER';
  campaignId?: string;
}

export interface CartLine {
  lineId: string;
  productCode: string;
  name: string;
  imageUrl?: string;
  quantity: number;
  price: {
    unitPrice: number;
    promoUnitPrice?: number | null;
    lineTotal: number;
  };
  availability: {
    status: AvailabilityStatus;
    reservedQuantity?: number | null;
    maxAllowedQuantity?: number | null;
    messageCode?: string | null;
  };
  source: string;
}

export interface AppliedOffer {
  offerId: string;
  offerType: string;
  status: string;
  benefitAmount: number;
  giftProductCode?: string | null;
  messageCode?: string | null;
}

export interface CartValidation {
  valid: boolean;
  blockingReasons: Array<{
    lineId?: string | null;
    code: string;
    messageCode: string;
  }>;
  checkoutRoute?: string | null;
}

export interface CartResponse {
  cartId: string;
  cartType: CartType;
  campaignId: string;
  roleSegment: string;
  partnerContextId?: string | null;
  status: CartStatus;
  currency: string;
  version: number;
  lines: CartLine[];
  appliedOffers: AppliedOffer[];
  totals: {
    subtotal: number;
    discountTotal: number;
    benefitTotal: number;
    shippingThresholdRemaining?: number | null;
    grandTotal: number;
  };
  validation: CartValidation;
  messageCode: string;
}

export interface ShoppingOffer {
  offerId: string;
  titleKey: string;
  offerType: string;
  status: 'AVAILABLE' | 'PENDING_CONDITION' | 'APPLIED' | 'UNAVAILABLE';
  requiredCondition?: string | null;
  remainingCondition?: string | null;
  relatedProductCodes: string[];
  benefitAmount?: number | null;
  messageCode: string;
}

export interface ShoppingOffersResponse {
  cartId: string;
  cartType: CartType;
  offers: ShoppingOffer[];
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
    'Idempotency-Key': idempotencyKey ?? `ui-${Date.now()}`,
  };
}

export async function loadCart(cartType: CartType = 'MAIN'): Promise<CartResponse> {
  const path = cartType === 'SUPPLEMENTARY' ? '/api/cart/supplementary/current' : '/api/cart/current';
  const response = await fetch(path, { headers: authHeaders() });
  return response.json();
}

export async function addCartItem(cartType: CartType, request: AddCartItemRequest): Promise<CartResponse> {
  const path = cartType === 'SUPPLEMENTARY' ? '/api/cart/supplementary/items' : '/api/cart/items';
  const response = await fetch(path, {
    method: 'POST',
    headers: jsonHeaders(),
    body: JSON.stringify(request),
  });
  return response.json();
}

export async function changeCartQuantity(lineId: string, quantity: number, expectedVersion: number): Promise<CartResponse> {
  const response = await fetch(`/api/cart/items/${lineId}`, {
    method: 'PATCH',
    headers: jsonHeaders(),
    body: JSON.stringify({ quantity, expectedVersion }),
  });
  return response.json();
}

export async function removeCartLine(lineId: string): Promise<CartResponse> {
  const response = await fetch(`/api/cart/items/${lineId}`, {
    method: 'DELETE',
    headers: jsonHeaders(),
  });
  return response.json();
}

export async function loadShoppingOffers(cartType: CartType = 'MAIN'): Promise<ShoppingOffersResponse> {
  const path = cartType === 'SUPPLEMENTARY' ? '/api/cart/supplementary/shopping-offers' : '/api/cart/shopping-offers';
  const response = await fetch(path, { headers: authHeaders() });
  return response.json();
}

export async function applyShoppingOffer(cartType: CartType, offerId: string): Promise<CartResponse> {
  const path = cartType === 'SUPPLEMENTARY'
    ? `/api/cart/supplementary/shopping-offers/${encodeURIComponent(offerId)}/apply`
    : `/api/cart/shopping-offers/${encodeURIComponent(offerId)}/apply`;
  const response = await fetch(path, {
    method: 'POST',
    headers: jsonHeaders(),
    body: '{}',
  });
  return response.json();
}

export async function loadSupportCart(userId: string, cartType: CartType): Promise<CartResponse> {
  const response = await fetch(`/api/cart/support/users/${encodeURIComponent(userId)}/current?cartType=${cartType}`, { headers: authHeaders() });
  return response.json();
}
