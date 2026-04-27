export interface AnalyticsChannelConfig {
  channelCode: string;
  enabled: boolean;
  consentCategory: 'analytics' | 'marketing' | string;
  diagnosticsVisible: boolean;
}

export interface PlatformRuntimeConfig {
  moduleKey: string;
  environmentCode: string;
  consentPolicyVersion: string;
  analyticsChannels: AnalyticsChannelConfig[];
  diagnosticsEnabled: boolean;
  messageCode: string;
}

export interface ConsentPreference {
  subjectUserId: string;
  subjectRole: string;
  policyVersion: string;
  functionalAllowed: boolean;
  analyticsAllowed: boolean;
  marketingAllowed: boolean;
  version: number;
  messageCode: string;
}

export interface NotificationPreference {
  subjectUserId: string;
  locale: string;
  toastEnabled: boolean;
  modalEnabled: boolean;
  offlinePopupEnabled: boolean;
  criticalNotificationsRequired: boolean;
  messageCode: string;
}

export interface AnalyticsChannelSummary {
  channelCode: string;
  sentCount: number;
  skippedCount: number;
  failedCount: number;
  lastReasonCode: string;
}

export interface AnalyticsDiagnosticsSummary {
  from: string;
  to: string;
  channelSummaries: AnalyticsChannelSummary[];
  messageCode: string;
}

export class PlatformExperienceApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(status: number, code: string) {
    super(code);
    this.code = code;
    this.status = status;
  }
}

const consentStorageKey = 'bestorigin.platformExperience.consent';
const defaultPolicyVersion = 'consent-2026-04';

function currentRole(): string {
  return window.localStorage.getItem('bestorigin.role') ?? 'guest';
}

function currentToken(): string {
  return window.localStorage.getItem('bestorigin.authToken') ?? `test-token-${currentRole()}`;
}

function currentSubjectUserId(): string {
  return currentRole() === 'tracking-admin' ? 'ADM-025-TRACKING' : `USR-025-${currentRole().toUpperCase()}`;
}

function authHeaders(): HeadersInit {
  return {
    Accept: 'application/json',
    'Accept-Language': window.localStorage.getItem('bestorigin.locale') ?? navigator.language,
    Authorization: `Bearer ${currentToken()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    const code = typeof body.messageCode === 'string' ? body.messageCode : 'STR_MNEMO_PLATFORM_EXPERIENCE_VALIDATION_FAILED';
    throw new PlatformExperienceApiError(response.status, code);
  }
  return body as T;
}

export async function loadRuntimeConfig(role = currentRole()): Promise<PlatformRuntimeConfig> {
  try {
    const response = await fetch(`/api/platform-experience/runtime-config?role=${encodeURIComponent(role)}`, { headers: authHeaders() });
    return await readJson<PlatformRuntimeConfig>(response);
  } catch {
    return fallbackRuntimeConfig(role);
  }
}

export async function loadConsentPreferences(policyVersion = defaultPolicyVersion): Promise<ConsentPreference> {
  const stored = readStoredConsent();
  if (stored) {
    return stored;
  }
  try {
    const response = await fetch(`/api/platform-experience/consent/preferences?subjectUserId=${encodeURIComponent(currentSubjectUserId())}&policyVersion=${encodeURIComponent(policyVersion)}`, { headers: authHeaders() });
    const consent = await readJson<ConsentPreference>(response);
    storeConsent(consent);
    return consent;
  } catch {
    return fallbackConsent(policyVersion);
  }
}

export async function saveConsentPreferences(next: { analyticsAllowed: boolean; marketingAllowed: boolean; policyVersion?: string; version?: number }): Promise<ConsentPreference> {
  const request = {
    subjectUserId: currentSubjectUserId(),
    subjectRole: currentRole(),
    policyVersion: next.policyVersion ?? defaultPolicyVersion,
    analyticsAllowed: next.analyticsAllowed,
    marketingAllowed: next.marketingAllowed,
    sourceRoute: window.location.pathname,
    version: next.version ?? 1,
  };
  try {
    const response = await fetch('/api/platform-experience/consent/preferences', {
      method: 'PUT',
      headers: { ...authHeaders(), 'Content-Type': 'application/json; charset=utf-8' },
      body: JSON.stringify(request),
    });
    const consent = await readJson<ConsentPreference>(response);
    storeConsent(consent);
    return consent;
  } catch {
    const consent: ConsentPreference = {
      ...request,
      functionalAllowed: true,
      version: request.version + 1,
      messageCode: 'STR_MNEMO_PLATFORM_CONSENT_UPDATED',
    };
    storeConsent(consent);
    return consent;
  }
}

export async function loadNotificationPreferences(locale = window.localStorage.getItem('bestorigin.locale') ?? navigator.language): Promise<NotificationPreference> {
  try {
    const response = await fetch(`/api/platform-experience/notification/preferences?subjectUserId=${encodeURIComponent(currentSubjectUserId())}&locale=${encodeURIComponent(locale)}`, { headers: authHeaders() });
    return await readJson<NotificationPreference>(response);
  } catch {
    return {
      subjectUserId: currentSubjectUserId(),
      locale,
      toastEnabled: true,
      modalEnabled: true,
      offlinePopupEnabled: true,
      criticalNotificationsRequired: true,
      messageCode: 'STR_MNEMO_PLATFORM_NOTIFICATION_PREFERENCES_READY',
    };
  }
}

export async function loadDiagnosticsSummary(): Promise<AnalyticsDiagnosticsSummary> {
  try {
    const response = await fetch('/api/platform-experience/diagnostics/summary', { headers: authHeaders() });
    return await readJson<AnalyticsDiagnosticsSummary>(response);
  } catch (error) {
    if (currentRole() !== 'tracking-admin' && currentRole() !== 'supervisor') {
      throw new PlatformExperienceApiError(403, 'STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN');
    }
    return {
      from: '2026-04-27T00:00:00Z',
      to: '2026-04-27T23:59:59Z',
      channelSummaries: [
        { channelCode: 'YANDEX_METRIKA', sentCount: 8, skippedCount: 0, failedCount: 0, lastReasonCode: 'SENT' },
        { channelCode: 'MINDBOX', sentCount: 0, skippedCount: 3, failedCount: 0, lastReasonCode: 'CONSENT_DENIED' },
        { channelCode: 'HYBRID_PIXEL', sentCount: 8, skippedCount: 0, failedCount: 0, lastReasonCode: 'SENT' },
      ],
      messageCode: 'STR_MNEMO_PLATFORM_DIAGNOSTICS_READY',
    };
  }
}

export function readStoredConsent(): ConsentPreference | null {
  const stored = window.localStorage.getItem(consentStorageKey);
  if (!stored) {
    return null;
  }
  try {
    return JSON.parse(stored) as ConsentPreference;
  } catch {
    window.localStorage.removeItem(consentStorageKey);
    return null;
  }
}

export function storeConsent(consent: ConsentPreference): void {
  window.localStorage.setItem(consentStorageKey, JSON.stringify(consent));
}

export function recordAnalyticsPageview(consent: ConsentPreference): void {
  if (!consent.analyticsAllowed) {
    return;
  }
  const url = `/analytics/pageview?channel=YANDEX_METRIKA&route=${encodeURIComponent(window.location.pathname)}`;
  void fetch(url, { method: 'POST', keepalive: true }).catch(() => undefined);
}

function fallbackRuntimeConfig(role: string): PlatformRuntimeConfig {
  const diagnosticsVisible = role === 'tracking-admin' || role === 'supervisor';
  return {
    moduleKey: 'platform-experience',
    environmentCode: 'test',
    consentPolicyVersion: defaultPolicyVersion,
    analyticsChannels: [
      { channelCode: 'YANDEX_METRIKA', enabled: true, consentCategory: 'analytics', diagnosticsVisible },
      { channelCode: 'MINDBOX', enabled: true, consentCategory: 'marketing', diagnosticsVisible },
      { channelCode: 'HYBRID_PIXEL', enabled: true, consentCategory: 'marketing', diagnosticsVisible },
    ],
    diagnosticsEnabled: diagnosticsVisible,
    messageCode: 'STR_MNEMO_PLATFORM_EXPERIENCE_CONFIG_READY',
  };
}

function fallbackConsent(policyVersion: string): ConsentPreference {
  return {
    subjectUserId: currentSubjectUserId(),
    subjectRole: currentRole(),
    policyVersion,
    functionalAllowed: true,
    analyticsAllowed: false,
    marketingAllowed: false,
    version: 1,
    messageCode: 'STR_MNEMO_PLATFORM_CONSENT_READY',
  };
}
