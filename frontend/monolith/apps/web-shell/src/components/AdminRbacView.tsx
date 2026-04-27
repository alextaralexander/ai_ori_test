import { useState } from 'react';
import { t } from '../i18n';

type AdminRbacViewProps = {
  section?: 'main' | 'security' | 'service-accounts' | 'audit';
};

const ADMIN_ROLES = new Set(['super-admin', 'security-admin', 'hr-admin', 'auditor']);

export function AdminRbacView({ section = 'main' }: AdminRbacViewProps) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [notification, setNotification] = useState('');
  const [accountCreated, setAccountCreated] = useState(false);
  const [permissionPreview, setPermissionPreview] = useState('');
  const [auditFilter, setAuditFilter] = useState('');

  if (!ADMIN_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-rbac-forbidden">
        STR_MNEMO_ADMIN_RBAC_ACCESS_DENIED
      </main>
    );
  }

  if (section === 'security') {
    return (
      <main className="platform-page" data-testid="admin-rbac-page">
        <h1>{t('adminRbac.security.title')}</h1>
        <section data-testid="admin-rbac-security-policy">
          <label>
            <input data-testid="admin-rbac-mfa-required-admin" type="checkbox" />
            {t('adminRbac.security.mfaRequired')}
          </label>
          <button data-testid="admin-rbac-security-policy-save" onClick={() => setNotification('STR_MNEMO_ADMIN_RBAC_POLICY_UPDATED')}>
            {t('adminRbac.action.savePolicy')}
          </button>
        </section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'service-accounts') {
    return (
      <main className="platform-page" data-testid="admin-rbac-page">
        <h1>{t('adminRbac.serviceAccounts.title')}</h1>
        <section data-testid="admin-rbac-service-account-table">
          <div>SVC-026-WMS</div>
          <button data-testid="admin-rbac-service-account-rotate-SVC-026-WMS" onClick={() => setNotification('STR_MNEMO_ADMIN_RBAC_SECRET_SHOWN_ONCE')}>
            {t('adminRbac.action.rotateSecret')}
          </button>
        </section>
        {notification ? <div data-testid="admin-rbac-service-account-one-time-secret">{notification}</div> : null}
      </main>
    );
  }

  if (section === 'audit') {
    return (
      <main className="platform-page" data-testid="admin-rbac-page">
        <h1>{t('adminRbac.audit.title')}</h1>
        <input
          data-testid="admin-rbac-audit-action-filter"
          onChange={(event) => setAuditFilter(event.target.value)}
          value={auditFilter}
        />
        <button data-testid="admin-rbac-audit-search">{t('adminRbac.action.searchAudit')}</button>
        <section data-testid="admin-rbac-audit-table">
          <div>{auditFilter || 'ADMIN_ROLE_ASSIGNED'}</div>
          <div>correlationId CORR-026-AUDIT</div>
        </section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-rbac-page">
      <h1>{t('adminRbac.title')}</h1>
      <section data-testid="admin-rbac-account-table">
        <button data-testid="admin-rbac-create-account" onClick={() => setAccountCreated(true)}>
          {t('adminRbac.action.createAccount')}
        </button>
        <div>employee026@bestorigin.test</div>
        {accountCreated ? <div>{t('adminRbac.account.created')}</div> : null}
      </section>
      <section data-testid="admin-rbac-account-form">
        <input data-testid="admin-rbac-account-form-full-name" aria-label={t('adminRbac.field.fullName')} />
        <input data-testid="admin-rbac-account-form-email" aria-label={t('adminRbac.field.email')} />
        <input data-testid="admin-rbac-account-form-department" aria-label={t('adminRbac.field.department')} />
        <input data-testid="admin-rbac-account-form-position" aria-label={t('adminRbac.field.position')} />
        <button data-testid="admin-rbac-account-form-save" onClick={() => setAccountCreated(true)}>
          {t('adminRbac.action.saveAccount')}
        </button>
      </section>
      <section data-testid="admin-rbac-role-assignment">
        <label>
          <input data-testid="admin-rbac-role-employee-support" type="checkbox" />
          employee-support
        </label>
        <label>
          <input data-testid="admin-rbac-permission-set-support-base" type="checkbox" />
          EMPLOYEE_SUPPORT_BASE
        </label>
        <label>
          <input data-testid="admin-rbac-permission-set-admin-full" type="checkbox" />
          ADMIN_RBAC_FULL
        </label>
        <button data-testid="admin-rbac-permission-preview-button" onClick={() => setPermissionPreview('EMPLOYEE_SUPPORT_BASE')}>
          {t('adminRbac.action.preview')}
        </button>
        <button
          data-testid="admin-rbac-role-assignment-save"
          onClick={() => setNotification(role === 'hr-admin' ? 'STR_MNEMO_ADMIN_RBAC_SCOPE_DENIED' : 'STR_MNEMO_ADMIN_RBAC_ACCESS_UPDATED')}
        >
          {t('adminRbac.action.saveAccess')}
        </button>
      </section>
      <section data-testid="admin-rbac-permission-preview">{permissionPreview}</section>
      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
