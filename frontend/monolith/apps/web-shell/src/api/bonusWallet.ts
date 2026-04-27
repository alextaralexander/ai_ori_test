export type BonusBucket = 'CASHBACK' | 'REFERRAL_DISCOUNT' | 'MANUAL_ADJUSTMENT' | 'ORDER_REDEMPTION';
export type BonusOperationType = 'ACCRUAL' | 'HOLD' | 'REDEMPTION' | 'REVERSAL' | 'EXPIRE' | 'MANUAL_ADJUSTMENT';
export type BonusTransactionStatus = 'ACTIVE' | 'HOLD' | 'REDEEMED' | 'REVERSED' | 'EXPIRED';

export interface BonusWalletBalanceResponse {
  bucket: BonusBucket;
  availableAmount: number;
  holdAmount: number;
  expiringSoonAmount: number;
  currencyCode: string;
}

export interface BonusWalletTransactionResponse {
  transactionId: string;
  bucket: BonusBucket;
  operationType: BonusOperationType;
  status: BonusTransactionStatus;
  amount: number;
  currencyCode: string;
  sourceType: string;
  sourceRef?: string | null;
  orderNumber?: string | null;
  claimId?: string | null;
  campaignId?: string | null;
  expiresAt?: string | null;
  publicMnemo?: string | null;
  correlationId: string;
  createdAt: string;
}

export interface BonusWalletApplyLimitResponse {
  orderNumber: string;
  availableAmount: number;
  maxApplicableAmount: number;
  blocked: boolean;
  reasonMnemo?: string | null;
}

export interface BonusWalletSummaryResponse {
  walletId: string;
  ownerUserId: string;
  currencyCode: string;
  balances: BonusWalletBalanceResponse[];
  recentTransactions: BonusWalletTransactionResponse[];
  applicationLimit: BonusWalletApplyLimitResponse;
  auditRecorded?: boolean;
}

export interface BonusWalletTransactionPageResponse {
  items: BonusWalletTransactionResponse[];
  page: number;
  size: number;
  totalElements: number;
  hasNext: boolean;
}

export interface BonusWalletTransactionDetailsResponse {
  transaction: BonusWalletTransactionResponse;
  linkedOrderUrl?: string | null;
  linkedClaimUrl?: string | null;
  auditRecorded?: boolean;
  events: Array<{
    eventType: string;
    publicStatus: string;
    sourceSystem: string;
    messageMnemo?: string | null;
    occurredAt: string;
  }>;
}

export interface BonusWalletExportResponse {
  exportId: string;
  status: string;
  format: string;
  rowsCount: number;
  messageMnemo: string;
}

interface ErrorResponse {
  code: string;
}

export class BonusWalletApiError extends Error {
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
    'Idempotency-Key': idempotencyKey ?? `ui-bonus-wallet-${Date.now()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as ErrorResponse;
    throw new BonusWalletApiError(response.status, error.code);
  }
  return body as T;
}

export async function getBonusWalletSummary(type: string): Promise<BonusWalletSummaryResponse> {
  const response = await fetch(`/api/bonus-wallet/summary?type=${encodeURIComponent(type)}`, {
    headers: authHeaders(),
  });
  return readJson<BonusWalletSummaryResponse>(response);
}

export async function searchBonusWalletTransactions(type: string, params: URLSearchParams): Promise<BonusWalletTransactionPageResponse> {
  const query = new URLSearchParams(params);
  query.set('type', type);
  const response = await fetch(`/api/bonus-wallet/transactions?${query.toString()}`, {
    headers: authHeaders(),
  });
  return readJson<BonusWalletTransactionPageResponse>(response);
}

export async function getBonusWalletTransaction(transactionId: string): Promise<BonusWalletTransactionDetailsResponse> {
  const response = await fetch(`/api/bonus-wallet/transactions/${encodeURIComponent(transactionId)}`, {
    headers: authHeaders(),
  });
  return readJson<BonusWalletTransactionDetailsResponse>(response);
}

export async function exportBonusWalletHistory(): Promise<BonusWalletExportResponse> {
  const response = await fetch('/api/bonus-wallet/exports', {
    method: 'POST',
    headers: jsonHeaders('ui-bonus-wallet-export'),
    body: JSON.stringify({ campaignId: 'CMP-2026-05', format: 'CSV' }),
  });
  return readJson<BonusWalletExportResponse>(response);
}

export async function getFinanceWallet(targetUserId: string): Promise<BonusWalletSummaryResponse> {
  const response = await fetch(`/api/bonus-wallet/finance/${encodeURIComponent(targetUserId)}?reason=FINANCE_REVIEW`, {
    headers: authHeaders(),
  });
  return readJson<BonusWalletSummaryResponse>(response);
}
