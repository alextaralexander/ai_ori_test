import { useState } from 'react';
import { t } from '../i18n';

type AdminPlatformSection = 'dashboard' | 'integrations' | 'audit';

const ADMIN_PLATFORM_ROLES = new Set(['business-admin', 'bi-analyst', 'integration-admin', 'audit-admin', 'super-admin']);

export function AdminPlatformView({ section = 'dashboard' }: { section?: AdminPlatformSection }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [notification, setNotification] = useState('');

  if (!ADMIN_PLATFORM_ROLES.has(role)) {
    return <main className="platform-page" data-testid="admin-platform-forbidden">STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED</main>;
  }

  if (section === 'integrations') {
    return (
      <main className="platform-page" data-testid="admin-platform-page">
        <h1>{t('adminPlatform.integrations.title')}</h1>
        <section data-testid="admin-platform-integrations">
          <div>WMS_1C DEGRADED SLA 20 CORR-036-WMS</div>
          <div>ASSEMBLY OK PAYMENT OK BONUS OK ANALYTICS STALE</div>
          <label>{t('adminPlatform.field.slaMinutes')}<input data-testid="admin-platform-integration-sla" defaultValue="20" /></label>
          <label>{t('adminPlatform.field.retryPolicy')}<select data-testid="admin-platform-integration-retry" defaultValue="EXPONENTIAL_3"><option value="EXPONENTIAL_3">EXPONENTIAL_3</option><option value="LINEAR_2">LINEAR_2</option></select></label>
          <label>{t('adminPlatform.field.reason')}<input data-testid="admin-platform-integration-reason" /></label>
          <button data-testid="admin-platform-integration-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED')}>{t('adminPlatform.action.saveIntegration')}</button>
        </section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'audit') {
    return (
      <main className="platform-page" data-testid="admin-platform-page">
        <h1>{t('adminPlatform.audit.title')}</h1>
        <select data-testid="admin-platform-audit-domain" aria-label={t('adminPlatform.field.domain')} defaultValue="INTEGRATION"><option value="INTEGRATION">INTEGRATION</option><option value="KPI">KPI</option></select>
        <button data-testid="admin-platform-audit-submit">{t('adminPlatform.action.search')}</button>
        <section data-testid="admin-platform-audit-table">
          <div>INTEGRATION_SETTINGS_SAVED maskedSubjectRef WMS*** correlationId CORR-036-AUDIT</div>
          <div>KPI_DASHBOARD_VIEWED maskedSubjectRef cam*** correlationId CORR-036-KPI</div>
        </section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-platform-page">
      <h1>{t('adminPlatform.title')}</h1>
      <section data-testid="admin-platform-kpi-board">
        <div>GMV 12500000 RUB</div>
        <div>CONVERSION 8.6%</div>
        <div>ORDERS 1842</div>
        <div>FULFILLMENT_SLA 96.2%</div>
      </section>
      <section data-testid="admin-platform-alerts">
        <div>STALE_KPI_SOURCE ANALYTICS</div>
        <div>INTEGRATION_SLA_BREACH WMS_1C</div>
      </section>
      <select data-testid="admin-platform-export-format" aria-label={t('adminPlatform.field.exportFormat')} defaultValue="XLSX"><option value="XLSX">XLSX</option><option value="CSV">CSV</option><option value="PDF">PDF</option></select>
      <button data-testid="admin-platform-export-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_PLATFORM_EXPORT_STARTED')}>{t('adminPlatform.action.export')}</button>
      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}