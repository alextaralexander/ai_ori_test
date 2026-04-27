export type PartnerReconciliationStatus = 'MATCHED' | 'MISMATCH' | 'PENDING';
export type PartnerReportDocumentStatus = 'DRAFT' | 'READY' | 'PUBLISHED' | 'REVOKED';
export type PartnerReportDocumentType = 'ACT' | 'RECEIPT' | 'CERTIFICATE' | 'PAYOUT_STATEMENT' | 'TAX_NOTE' | 'RECONCILIATION_REPORT';

export interface MoneyAmount {
  amount: number;
  currencyCode: string;
}

export interface PartnerReportTotals {
  grossSales: MoneyAmount;
  commissionBase: MoneyAmount;
  accruedCommission: MoneyAmount;
  withheld: MoneyAmount;
  payable: MoneyAmount;
  paid: MoneyAmount;
}

export interface PartnerReportSummaryResponse {
  reportPeriodId: string;
  partnerId: string;
  catalogId: string;
  bonusProgramId?: string | null;
  totals: PartnerReportTotals;
  reconciliationStatus: PartnerReconciliationStatus;
  publicMnemo?: string | null;
  correlationId: string;
}

export interface PartnerReportOrderLineResponse {
  orderNumber: string;
  orderSource: string;
  structureLevel?: number | null;
  orderedAt: string;
  orderAmount: MoneyAmount;
  commissionBase: MoneyAmount;
  commissionRatePercent: number;
  commissionAmount: MoneyAmount;
  calculationStatus: string;
  payoutReference?: string | null;
}

export interface PartnerReportOrderPageResponse {
  items: PartnerReportOrderLineResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface PartnerCommissionAdjustmentResponse {
  adjustmentType: string;
  reasonCode: string;
  sourceRef: string;
  amount: MoneyAmount;
}

export interface PartnerCommissionDetailResponse {
  orderLine: PartnerReportOrderLineResponse;
  adjustments: PartnerCommissionAdjustmentResponse[];
  payoutReference?: string | null;
  publicMnemo?: string | null;
  correlationId: string;
}

export interface PartnerReportDocumentResponse {
  documentId: string;
  documentCode: string;
  documentType: PartnerReportDocumentType;
  documentStatus: PartnerReportDocumentStatus;
  versionNumber: number;
  checksumSha256: string;
  publishedAt?: string | null;
  publicMnemo?: string | null;
}

export interface PartnerReportDocumentPageResponse {
  items: PartnerReportDocumentResponse[];
  page: number;
  size: number;
  totalElements: number;
}

export interface PartnerReportDocumentDownloadResponse extends PartnerReportDocumentResponse {
  downloadUrl: string;
  expiresAt: string;
}

export interface PartnerReportPrintViewResponse {
  documentId: string;
  documentCode: string;
  versionNumber: number;
  checksumSha256: string;
  printViewUrl: string;
}

export interface PartnerReportExportResponse {
  exportId: string;
  exportStatus: string;
  format: string;
  rowCount: number;
  publicMnemo: string;
}

export interface PartnerReportFinanceReconciliationResponse extends PartnerReportSummaryResponse {
  mismatchReasons: string[];
  auditRecorded: boolean;
  reason: string;
}

interface ErrorResponse {
  code: string;
}

export class PartnerReportApiError extends Error {
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
    'Idempotency-Key': idempotencyKey ?? `ui-partner-report-${Date.now()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as ErrorResponse;
    throw new PartnerReportApiError(response.status, error.code);
  }
  return body as T;
}

export async function getPartnerReportSummary(params: URLSearchParams): Promise<PartnerReportSummaryResponse> {
  const response = await fetch(`/api/partner-reporting/reports/summary?${params.toString()}`, { headers: authHeaders() });
  return readJson<PartnerReportSummaryResponse>(response);
}

export async function searchPartnerReportOrders(params: URLSearchParams): Promise<PartnerReportOrderPageResponse> {
  const response = await fetch(`/api/partner-reporting/reports/orders?${params.toString()}`, { headers: authHeaders() });
  return readJson<PartnerReportOrderPageResponse>(response);
}

export async function getPartnerCommissionDetails(orderNumber: string): Promise<PartnerCommissionDetailResponse> {
  const response = await fetch(`/api/partner-reporting/reports/orders/${encodeURIComponent(orderNumber)}/commission`, { headers: authHeaders() });
  return readJson<PartnerCommissionDetailResponse>(response);
}

export async function getPartnerReportDocuments(params: URLSearchParams): Promise<PartnerReportDocumentPageResponse> {
  const response = await fetch(`/api/partner-reporting/documents?${params.toString()}`, { headers: authHeaders() });
  return readJson<PartnerReportDocumentPageResponse>(response);
}

export async function downloadPartnerReportDocument(documentId: string): Promise<PartnerReportDocumentDownloadResponse> {
  const response = await fetch(`/api/partner-reporting/documents/${encodeURIComponent(documentId)}/download`, {
    method: 'POST',
    headers: jsonHeaders(`ui-partner-report-download-${documentId}`),
    body: '{}',
  });
  return readJson<PartnerReportDocumentDownloadResponse>(response);
}

export async function getPartnerReportPrintView(documentId: string): Promise<PartnerReportPrintViewResponse> {
  const response = await fetch(`/api/partner-reporting/documents/${encodeURIComponent(documentId)}/print-view`, { headers: authHeaders() });
  return readJson<PartnerReportPrintViewResponse>(response);
}

export async function exportPartnerReport(): Promise<PartnerReportExportResponse> {
  const response = await fetch('/api/partner-reporting/exports', {
    method: 'POST',
    headers: jsonHeaders('ui-partner-report-export'),
    body: JSON.stringify({ format: 'XLSX', dateFrom: '2026-05-01', dateTo: '2026-05-21', catalogId: 'CAT-2026-05', bonusProgramId: 'MLM-BASE' }),
  });
  return readJson<PartnerReportExportResponse>(response);
}

export async function getPartnerFinanceReconciliation(partnerId: string): Promise<PartnerReportFinanceReconciliationResponse> {
  const response = await fetch(`/api/partner-reporting/finance/reconciliations?partnerId=${encodeURIComponent(partnerId)}&dateFrom=2026-05-01&dateTo=2026-05-21&reason=MONTHLY_CONTROL`, { headers: authHeaders() });
  return readJson<PartnerReportFinanceReconciliationResponse>(response);
}

export async function revokePartnerReportDocument(documentId: string): Promise<PartnerReportDocumentResponse> {
  const response = await fetch(`/api/partner-reporting/finance/documents/${encodeURIComponent(documentId)}/revoke`, {
    method: 'POST',
    headers: jsonHeaders(`ui-partner-report-revoke-${documentId}`),
    body: JSON.stringify({ reasonCode: 'WRONG_AMOUNT' }),
  });
  return readJson<PartnerReportDocumentResponse>(response);
}
