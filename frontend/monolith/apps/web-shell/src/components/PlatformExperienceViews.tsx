import { Alert, Button, Space } from 'antd';
import type { ReactElement, ReactNode } from 'react';
import { useEffect, useState } from 'react';
import { loadConsentPreferences, loadDiagnosticsSummary, loadNotificationPreferences, loadRuntimeConfig, PlatformExperienceApiError, recordAnalyticsPageview, saveConsentPreferences, type AnalyticsDiagnosticsSummary, type ConsentPreference } from '../api/platformExperience';
import { getCurrentLocale, setCurrentLocale, t } from '../i18n';

interface PlatformExperienceShellProps {
  children: ReactNode;
  showLanguageSwitcher?: boolean;
}

export function PlatformExperienceShell({ children, showLanguageSwitcher = false }: PlatformExperienceShellProps): ReactElement {
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [offline, setOffline] = useState(!navigator.onLine);
  const [reconnected, setReconnected] = useState(false);
  const [locale, setLocale] = useState(getCurrentLocale());
  const [languageOpen, setLanguageOpen] = useState(false);

  useEffect(() => {
    void loadNotificationPreferences(locale);
    function markOffline(): void {
      setOffline(true);
      setReconnected(false);
    }
    function markOnline(): void {
      setOffline(false);
      setReconnected(true);
    }
    window.addEventListener('offline', markOffline);
    window.addEventListener('online', markOnline);
    return () => {
      window.removeEventListener('offline', markOffline);
      window.removeEventListener('online', markOnline);
    };
  }, [locale]);

  function chooseLocale(nextLocale: 'ru' | 'en'): void {
    setCurrentLocale(nextLocale);
    setLocale(nextLocale);
    setLanguageOpen(false);
  }

  return (
    <>
      {showLanguageSwitcher ? (
        <section className="platform-language-panel">
          <Button data-testid="platform-language-switcher" onClick={() => setLanguageOpen((open) => !open)}>
            {t('platform.language.switcher')}
          </Button>
          {languageOpen ? (
            <Space className="platform-language-options">
              <Button data-testid="platform-language-option-ru" onClick={() => chooseLocale('ru')}>RU</Button>
              <Button data-testid="platform-language-option-en" onClick={() => chooseLocale('en')}>EN</Button>
            </Space>
          ) : null}
          <span data-testid="platform-i18n-current-locale">{locale}</span>
        </section>
      ) : null}
      <section className="platform-notification-root" data-testid="platform-notification-root">
        <Button data-testid="platform-demo-success-notification" onClick={() => setMessageCode('STR_MNEMO_PLATFORM_NOTIFICATION_DEMO_SUCCESS')}>
          {t('platform.notification.demo')}
        </Button>
        {messageCode ? <PlatformMessage code={messageCode} /> : null}
        {reconnected ? (
          <div className="platform-reconnect-notification" data-testid="platform-reconnect-notification">
            <PlatformMessage code="STR_MNEMO_PLATFORM_RECONNECTED" />
          </div>
        ) : null}
      </section>
      {offline ? (
        <aside className="platform-offline-popup" data-testid="platform-offline-popup">
          <PlatformMessage code="STR_MNEMO_PLATFORM_OFFLINE" />
        </aside>
      ) : null}
      {children}
    </>
  );
}

export function PlatformConsentView(): ReactElement {
  const [consent, setConsent] = useState<ConsentPreference | null>(null);
  const [analyticsAllowed, setAnalyticsAllowed] = useState(false);
  const [marketingAllowed, setMarketingAllowed] = useState(false);

  useEffect(() => {
    let active = true;
    async function load(): Promise<void> {
      const runtime = await loadRuntimeConfig();
      const loaded = await loadConsentPreferences(runtime.consentPolicyVersion);
      if (!active) {
        return;
      }
      setConsent(loaded);
      setAnalyticsAllowed(loaded.analyticsAllowed);
      setMarketingAllowed(loaded.marketingAllowed);
    }
    void load();
    return () => {
      active = false;
    };
  }, []);

  async function save(): Promise<void> {
    const updated = await saveConsentPreferences({
      analyticsAllowed,
      marketingAllowed,
      policyVersion: consent?.policyVersion,
      version: consent?.version,
    });
    setConsent(updated);
  }

  return (
    <main className="platform-page" data-testid="platform-consent-panel">
      <h1>{t('platform.consent.title')}</h1>
      <p>{t('platform.consent.description')}</p>
      <Space direction="vertical">
        <label className="platform-checkbox">
          <input checked disabled type="checkbox" />
          <span>{t('platform.consent.functional')}</span>
        </label>
        <label className="platform-checkbox">
          <input checked={analyticsAllowed} data-testid="platform-consent-analytics" onChange={(event) => setAnalyticsAllowed(event.target.checked)} type="checkbox" />
          <span>{t('platform.consent.analytics')}</span>
        </label>
        <label className="platform-checkbox">
          <input checked={marketingAllowed} data-testid="platform-consent-marketing" onChange={(event) => setMarketingAllowed(event.target.checked)} type="checkbox" />
          <span>{t('platform.consent.marketing')}</span>
        </label>
        <Button data-testid="platform-consent-save" onClick={() => void save()} type="primary">
          {t('platform.consent.save')}
        </Button>
      </Space>
      <PlatformConsentState consent={consent} />
    </main>
  );
}

export function PlatformConsentState({ consent: externalConsent }: { consent?: ConsentPreference | null }): ReactElement {
  const [consent, setConsent] = useState<ConsentPreference | null>(externalConsent ?? null);

  useEffect(() => {
    if (externalConsent !== undefined) {
      setConsent(externalConsent);
      return;
    }
    let active = true;
    loadConsentPreferences().then((loaded) => {
      if (!active) {
        return;
      }
      setConsent(loaded);
      recordAnalyticsPageview(loaded);
    });
    return () => {
      active = false;
    };
  }, [externalConsent]);

  const state = consent
    ? [
      consent.functionalAllowed ? 'functional' : '',
      consent.analyticsAllowed ? 'analytics' : '',
      consent.marketingAllowed ? 'marketing' : '',
    ].filter(Boolean).join(' ')
    : 'functional';

  return <div className="platform-consent-state" data-testid="platform-consent-panel-state">{state}</div>;
}

export function PlatformAnalyticsDiagnosticsView(): ReactElement {
  const [summary, setSummary] = useState<AnalyticsDiagnosticsSummary | null>(null);
  const [forbiddenCode, setForbiddenCode] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    loadDiagnosticsSummary()
      .then((loaded) => {
        if (active) {
          setSummary(loaded);
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setForbiddenCode(error instanceof PlatformExperienceApiError ? error.code : 'STR_MNEMO_ANALYTICS_DIAGNOSTICS_FORBIDDEN');
        }
      });
    return () => {
      active = false;
    };
  }, []);

  if (forbiddenCode) {
    return (
      <main className="platform-page">
        <Alert data-testid="platform-diagnostics-forbidden" message={`${t(forbiddenCode)} (${forbiddenCode})`} type="error" />
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="platform-analytics-diagnostics">
      <h1>{t('platform.diagnostics.title')}</h1>
      <section className="platform-diagnostics-grid">
        {(summary?.channelSummaries ?? []).map((item) => (
          <article className="platform-diagnostics-card" key={item.channelCode}>
            <strong>{item.channelCode}</strong>
            <span>{t('platform.diagnostics.sent')}: {item.sentCount}</span>
            <span>{t('platform.diagnostics.skipped')}: {item.skippedCount}</span>
            <span>{t('platform.diagnostics.failed')}: {item.failedCount}</span>
            <span>{item.lastReasonCode}</span>
          </article>
        ))}
      </section>
    </main>
  );
}

function PlatformMessage({ code }: { code: string }): ReactElement {
  return (
    <span className="platform-message">
      <strong>{code}</strong>
      <span>{t(code)}</span>
    </span>
  );
}
