import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement, ReactNode } from 'react';
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  AuthApiError,
  finishImpersonation,
  getCurrentSession,
  login,
  logout,
  saveInvitationCode,
  searchPartners,
  setActivePartner,
  startImpersonation,
  type AuthImpersonationResponse,
  type AuthPartnerOptionResponse,
  type AuthSessionResponse,
} from '../api/auth';
import { t } from '../i18n';

interface AuthContextValue {
  session: AuthSessionResponse | null;
  reload: () => Promise<AuthSessionResponse>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }): ReactElement {
  const [session, setSession] = useState<AuthSessionResponse | null>(null);

  const reload = useCallback(async (): Promise<AuthSessionResponse> => {
    const loaded = await getCurrentSession();
    setSession(loaded);
    return loaded;
  }, []);

  const value = useMemo(() => ({ session, reload }), [session]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('AuthProvider is required');
  }
  return value;
}

export function useInvitationCode() {
  return { saveInvitationCode };
}

export function useActivePartner() {
  return { setActivePartner };
}

export function usePartnerSearch() {
  return { searchPartners };
}

export function useSuperUserMode() {
  return { active: Boolean(window.localStorage.getItem('bestorigin.impersonationSessionId')) };
}

export function useImpersonate() {
  return { startImpersonation, finishImpersonation };
}

export function AuthSessionView(): ReactElement {
  const { reload } = useAuth();
  const [session, setSession] = useState<AuthSessionResponse | null>(null);
  const [query, setQuery] = useState('024');
  const [partners, setPartners] = useState<AuthPartnerOptionResponse[]>([]);
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    const invitationCode = window.localStorage.getItem('bestorigin.invitationCode') ?? 'INV-024-SPONSOR';
    void reload()
      .then(async (loaded) => {
        const invitationCodeState = await saveInvitationCode(invitationCode);
        setSession({ ...loaded, invitationCodeState });
      })
      .catch((error: unknown) => setMessageCode(error instanceof AuthApiError ? error.code : 'STR_MNEMO_AUTH_SESSION_EXPIRED'));
  }, [reload]);

  async function findPartners(): Promise<void> {
    setPartners((await searchPartners(query)).items);
  }

  async function choosePartner(partnerId: string): Promise<void> {
    setSession(await setActivePartner(partnerId));
  }

  async function logoutClick(): Promise<void> {
    await logout();
    const loaded = await login('guest');
    setSession(loaded);
  }

  if (messageCode) {
    return <Alert data-testid="auth-route-forbidden" title={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  return (
    <main className="employee-page">
      <header className="employee-heading">
        <h1>{t('auth.session.title')}</h1>
        <span>{t('auth.session.description')}</span>
      </header>
      <section className="employee-card" data-testid="auth-role-router">
        <strong>{session?.roles.join(', ') ?? 'guest'}</strong>
        <span>{session?.defaultRoute ?? '/'}</span>
      </section>
      <section className="employee-card" data-testid="auth-invitation-code-state">
        <span>{session?.invitationCodeState.status ?? 'NOT_PROVIDED'}</span>
        <span>{session?.invitationCodeState.invitationCode}</span>
      </section>
      <section className="employee-card employee-form" data-testid="auth-partner-search">
        <Input data-testid="auth-partner-search-input" value={query} onChange={(event) => setQuery(event.target.value)} />
        <Button data-testid="auth-partner-search-submit" onClick={() => void findPartners()} type="primary">{t('auth.partner.search')}</Button>
        {partners.map((partner) => (
          <Button data-testid={`auth-active-partner-select-${partner.partnerId}`} key={partner.partnerId} onClick={() => void choosePartner(partner.partnerId)}>
            {partner.partnerId} {partner.personNumber}
          </Button>
        ))}
      </section>
      <section className="employee-card" data-testid="auth-active-partner-switcher">
        {session?.activePartner?.partnerId ?? t('auth.partner.empty')}
      </section>
      <Button data-testid="auth-logout-button" onClick={() => void logoutClick()}>{t('auth.logout')}</Button>
    </main>
  );
}

export function AuthImpersonationView(): ReactElement {
  const [targetUserId, setTargetUserId] = useState('USR-024-PARTNER');
  const [targetRole, setTargetRole] = useState('partner');
  const [reasonCode, setReasonCode] = useState('SUPPORT_CASE_REVIEW');
  const [reasonText, setReasonText] = useState(t('auth.impersonation.defaultReason'));
  const [durationMinutes, setDurationMinutes] = useState('20');
  const [impersonation, setImpersonation] = useState<AuthImpersonationResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);

  async function start(): Promise<void> {
    try {
      setImpersonation(await startImpersonation({ targetUserId, targetRole, reasonCode, reasonText, durationMinutes: Number(durationMinutes) }));
      setMessageCode(null);
    } catch (error) {
      setMessageCode(error instanceof AuthApiError ? error.code : 'STR_MNEMO_AUTH_IMPERSONATION_INVALID');
    }
  }

  async function finish(): Promise<void> {
    if (!impersonation) {
      return;
    }
    await finishImpersonation(impersonation.impersonationSessionId);
    setImpersonation(null);
  }

  return (
    <main className="employee-page">
      <header className="employee-heading">
        <h1>{t('auth.impersonation.title')}</h1>
        <span>{t('auth.impersonation.description')}</span>
      </header>
      {messageCode ? <Alert data-testid="auth-impersonation-error" title={`${t(messageCode)} (${messageCode})`} type="error" /> : null}
      {impersonation ? (
        <section className="employee-card" data-testid="auth-impersonation-banner">
          <span>{impersonation.targetUserId}</span>
          <span>{impersonation.targetRole}</span>
          <span>{impersonation.reasonCode}</span>
          <Button data-testid="auth-impersonation-finish" onClick={() => void finish()}>{t('auth.impersonation.finish')}</Button>
        </section>
      ) : null}
      <section className="employee-card employee-form" data-testid="auth-impersonation-panel">
        <Input data-testid="auth-impersonation-target-user-id" value={targetUserId} onChange={(event) => setTargetUserId(event.target.value)} />
        <Input data-testid="auth-impersonation-target-role" value={targetRole} onChange={(event) => setTargetRole(event.target.value)} />
        <Input data-testid="auth-impersonation-reason-code" value={reasonCode} onChange={(event) => setReasonCode(event.target.value)} />
        <Input data-testid="auth-impersonation-reason-text" value={reasonText} onChange={(event) => setReasonText(event.target.value)} />
        <Input data-testid="auth-impersonation-duration" value={durationMinutes} onChange={(event) => setDurationMinutes(event.target.value)} />
        <Space>
          <Button data-testid="auth-impersonation-start" onClick={() => void start()} type="primary">{t('auth.impersonation.start')}</Button>
        </Space>
      </section>
    </main>
  );
}

export function AuthRouteForbidden({ code = 'STR_MNEMO_AUTH_ACCESS_DENIED' }: { code?: string }): ReactElement {
  return <Alert data-testid="auth-route-forbidden" title={`${t(code)} (${code})`} type="error" />;
}
