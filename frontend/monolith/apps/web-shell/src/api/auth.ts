export interface AuthRoutePolicyResponse {
  routePattern: string;
  roleCode: string;
  moduleKey: string;
  allowed: boolean;
  deniedCode?: string | null;
}

export interface AuthInvitationCodeResponse {
  invitationCode?: string | null;
  status: 'VALID' | 'EXPIRED' | 'USED' | 'UNKNOWN' | 'NOT_PROVIDED';
  warningCode?: string | null;
  auditRecorded: boolean;
}

export interface AuthPartnerOptionResponse {
  partnerId: string;
  personNumber: string;
  displayName: string;
  roleInStructure: string;
  scopeCode: string;
}

export interface AuthActivePartnerResponse extends AuthPartnerOptionResponse {
  selectedAt: string;
}

export interface AuthImpersonationResponse {
  impersonationSessionId: string;
  actorUserId: string;
  targetUserId: string;
  targetRole: string;
  reasonCode: string;
  status: 'ACTIVE' | 'FINISHED' | 'EXPIRED' | 'REVOKED';
  startedAt: string;
  expiresAt: string;
  auditRecorded: boolean;
}

export interface AuthSessionResponse {
  token: string;
  userId: string;
  displayName: string;
  roles: string[];
  defaultRoute: string;
  routePolicies: AuthRoutePolicyResponse[];
  activePartner?: AuthActivePartnerResponse | null;
  invitationCodeState: AuthInvitationCodeResponse;
  impersonation?: AuthImpersonationResponse | null;
  auditRecorded: boolean;
}

export interface AuthPartnerSearchResponse {
  items: AuthPartnerOptionResponse[];
  auditRecorded: boolean;
}

interface AuthErrorResponse {
  code: string;
}

export class AuthApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(status: number, code: string) {
    super(code);
    this.code = code;
    this.status = status;
  }
}

function authToken(): string {
  const stored = window.localStorage.getItem('bestorigin.authToken');
  if (stored) {
    return stored;
  }
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  return `test-token-${role}`;
}

function authHeaders(): HeadersInit {
  return {
    Accept: 'application/json',
    'Accept-Language': 'ru-RU',
    Authorization: `Bearer ${authToken()}`,
  };
}

async function readJson<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    const error = body as AuthErrorResponse;
    throw new AuthApiError(response.status, error.code);
  }
  return body as T;
}

export async function login(role: string, invitationCode?: string | null): Promise<AuthSessionResponse> {
  const response = await fetch('/api/auth/test-login', {
    method: 'POST',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify({ username: `${role}-user`, role, invitationCode }),
  });
  const session = await readJson<AuthSessionResponse>(response);
  window.localStorage.setItem('bestorigin.authToken', session.token);
  window.localStorage.setItem('bestorigin.role', role);
  if (session.activePartner?.partnerId) {
    window.localStorage.setItem('bestorigin.activePartnerId', session.activePartner.partnerId);
  }
  return session;
}

export async function getCurrentSession(): Promise<AuthSessionResponse> {
  const response = await fetch('/api/auth/session', { headers: authHeaders() });
  return readJson<AuthSessionResponse>(response);
}

export async function logout(): Promise<void> {
  await readJson<{ loggedOut: boolean; auditRecorded: boolean }>(await fetch('/api/auth/session', { method: 'DELETE', headers: authHeaders() }));
  window.localStorage.removeItem('bestorigin.authToken');
  window.localStorage.removeItem('bestorigin.activePartnerId');
  window.localStorage.removeItem('bestorigin.invitationCode');
  window.localStorage.removeItem('bestorigin.impersonationSessionId');
  window.localStorage.setItem('bestorigin.role', 'guest');
}

export async function saveInvitationCode(invitationCode: string): Promise<AuthInvitationCodeResponse> {
  const response = await fetch('/api/auth/invitation-code', {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify({ invitationCode }),
  });
  const state = await readJson<AuthInvitationCodeResponse>(response);
  if (state.invitationCode) {
    window.localStorage.setItem('bestorigin.invitationCode', state.invitationCode);
  }
  return state;
}

export async function searchPartners(query: string): Promise<AuthPartnerSearchResponse> {
  const response = await fetch(`/api/auth/partners/search?query=${encodeURIComponent(query)}`, { headers: authHeaders() });
  return readJson<AuthPartnerSearchResponse>(response);
}

export async function setActivePartner(partnerId: string): Promise<AuthSessionResponse> {
  const response = await fetch('/api/auth/partners/active', {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify({ partnerId }),
  });
  const session = await readJson<AuthSessionResponse>(response);
  if (session.activePartner?.partnerId) {
    window.localStorage.setItem('bestorigin.activePartnerId', session.activePartner.partnerId);
  }
  return session;
}

export async function startImpersonation(payload: { targetUserId: string; targetRole: string; reasonCode: string; reasonText: string; durationMinutes: number }): Promise<AuthImpersonationResponse> {
  const response = await fetch('/api/auth/impersonation', {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json; charset=utf-8', 'X-Elevated-Session-Id': '02400000-0000-0000-0000-000000000001' },
    body: JSON.stringify(payload),
  });
  const impersonation = await readJson<AuthImpersonationResponse>(response);
  window.localStorage.setItem('bestorigin.impersonationSessionId', impersonation.impersonationSessionId);
  return impersonation;
}

export async function finishImpersonation(impersonationSessionId: string): Promise<AuthSessionResponse> {
  const response = await fetch(`/api/auth/impersonation/${encodeURIComponent(impersonationSessionId)}/finish`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json; charset=utf-8' },
    body: '{}',
  });
  window.localStorage.removeItem('bestorigin.impersonationSessionId');
  return readJson<AuthSessionResponse>(response);
}
