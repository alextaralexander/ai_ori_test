export interface MlmVolume {
  amount: number;
  currencyCode: string;
}

export interface MlmDashboardResponse {
  campaignId: string;
  leaderPersonNumber: string;
  personalVolume: MlmVolume;
  groupVolume: MlmVolume;
  activePartnerCount: number;
  currentRank: string;
  nextRank: string;
  qualificationPercent: number;
  nextActions: string[];
  publicMnemo: string;
}

export interface MlmPartnerNodeResponse {
  personNumber: string;
  displayName: string;
  branchId: string;
  structureLevel: number;
  partnerRole: string;
  partnerStatus: string;
  personalVolume: MlmVolume;
  groupVolume: MlmVolume;
  riskScore: number;
}

export interface MlmCommunityResponse {
  campaignId: string;
  partners: MlmPartnerNodeResponse[];
  totalElements: number;
  publicMnemo: string;
}

export interface MlmConversionFunnelResponse {
  campaignId: string;
  inviteSentCount: number;
  inviteAcceptedCount: number;
  registeredCount: number;
  activatedCount: number;
  firstOrderCount: number;
  conversionRatePercent: number;
  publicMnemo: string;
}

export interface MlmTeamActivityItemResponse {
  personNumber: string;
  activityType: string;
  activityStatus: string;
  occurredAt: string;
  riskSignal: boolean;
  publicMnemo: string;
}

export interface MlmTeamActivityResponse {
  campaignId: string;
  items: MlmTeamActivityItemResponse[];
  totalElements: number;
  publicMnemo: string;
}

export interface MlmUpgradeRequirementResponse {
  code: string;
  status: string;
  currentValue: number;
  targetValue: number;
  publicMnemo: string;
}

export interface MlmUpgradeResponse {
  campaignId: string;
  personNumber: string;
  currentRank: string;
  nextRank: string;
  qualificationProgress: number;
  deadlineAt: string;
  requirements: MlmUpgradeRequirementResponse[];
  publicMnemo: string;
}

export interface MlmPartnerCardResponse extends MlmPartnerNodeResponse {
  sponsorPersonNumber?: string | null;
  qualificationProgress: MlmUpgradeResponse;
  linkedActions: Record<string, string>;
  publicMnemo: string;
}

interface ErrorResponse {
  code: string;
}

export class MlmStructureApiError extends Error {
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

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as ErrorResponse;
    throw new MlmStructureApiError(response.status, error.code);
  }
  return body as T;
}

export async function getMlmDashboard(params: URLSearchParams): Promise<MlmDashboardResponse> {
  const response = await fetch(`/api/mlm-structure/dashboard?${params.toString()}`, { headers: authHeaders() });
  return readJson<MlmDashboardResponse>(response);
}

export async function getMlmCommunity(params: URLSearchParams): Promise<MlmCommunityResponse> {
  const response = await fetch(`/api/mlm-structure/community?${params.toString()}`, { headers: authHeaders() });
  return readJson<MlmCommunityResponse>(response);
}

export async function getMlmConversion(params: URLSearchParams): Promise<MlmConversionFunnelResponse> {
  const response = await fetch(`/api/mlm-structure/conversion?${params.toString()}`, { headers: authHeaders() });
  return readJson<MlmConversionFunnelResponse>(response);
}

export async function getMlmTeamActivity(params: URLSearchParams): Promise<MlmTeamActivityResponse> {
  const response = await fetch(`/api/mlm-structure/team-activity?${params.toString()}`, { headers: authHeaders() });
  return readJson<MlmTeamActivityResponse>(response);
}

export async function getMlmUpgrade(params: URLSearchParams): Promise<MlmUpgradeResponse> {
  const response = await fetch(`/api/mlm-structure/upgrade?${params.toString()}`, { headers: authHeaders() });
  return readJson<MlmUpgradeResponse>(response);
}

export async function getMlmPartnerCard(personNumber: string, params: URLSearchParams): Promise<MlmPartnerCardResponse> {
  const response = await fetch(`/api/mlm-structure/partners/${encodeURIComponent(personNumber)}?${params.toString()}`, { headers: authHeaders() });
  return readJson<MlmPartnerCardResponse>(response);
}
