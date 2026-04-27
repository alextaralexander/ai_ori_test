import { useState } from 'react';
import { t } from '../i18n';

type AdminIdentitySection = 'subjects' | 'partner' | 'impersonation' | 'audit';

const ADMIN_IDENTITY_ROLES = new Set([
  'master-data-admin',
  'partner-ops-admin',
  'employee-admin',
  'security-admin',
  'personal-data-auditor',
  'super-admin',
]);

export function AdminIdentityView({ section = 'subjects' }: { section?: AdminIdentitySection }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [cardOpen, setCardOpen] = useState(false);
  const [notification, setNotification] = useState('');
  const [impersonating, setImpersonating] = useState(false);
  const [sponsorPreview, setSponsorPreview] = useState(false);

  if (!ADMIN_IDENTITY_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-identity-forbidden">
        STR_MNEMO_ADMIN_IDENTITY_FORBIDDEN_ACTION
      </main>
    );
  }

  if (section === 'partner') {
    return (
      <main className="platform-page" data-testid="admin-identity-page">
        <h1>{t('adminIdentity.partner.title')}</h1>
        <section data-testid="admin-identity-partner-panel">
          <div>PTR-035-1001 SP-035-A Office 17 Leader</div>
          <button data-testid="admin-identity-transfer-sponsor">{t('adminIdentity.action.transferSponsor')}</button>
          <input data-testid="admin-identity-new-sponsor" aria-label={t('adminIdentity.field.newSponsor')} />
          <input data-testid="admin-identity-sponsor-effective-from" aria-label={t('adminIdentity.field.effectiveFrom')} />
          <select data-testid="admin-identity-sponsor-reason" aria-label={t('adminIdentity.field.reason')}>
            <option value="PARTNER_TRANSFER_APPROVED">PARTNER_TRANSFER_APPROVED</option>
          </select>
          <button data-testid="admin-identity-sponsor-preview" onClick={() => setSponsorPreview(true)}>
            {t('adminIdentity.action.preview')}
          </button>
          {sponsorPreview ? <div data-testid="admin-identity-sponsor-impact-preview">downlineCount: 42</div> : null}
          <button data-testid="admin-identity-sponsor-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_IDENTITY_SPONSOR_CHANGED')}>
            {t('adminIdentity.action.save')}
          </button>
        </section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'impersonation') {
    return (
      <main className="platform-page" data-testid="admin-identity-impersonation-page">
        <h1>{t('adminIdentity.impersonation.title')}</h1>
        {impersonating ? (
          <section data-testid="admin-identity-impersonation-banner">
            {t('adminIdentity.impersonation.banner')} USR-035-1007 SUPPORT_DIAGNOSTICS
          </section>
        ) : null}
        <section>
          <input data-testid="admin-identity-policy-code" aria-label={t('adminIdentity.field.policyCode')} />
          <select data-testid="admin-identity-policy-target-type" aria-label={t('adminIdentity.field.targetType')}>
            <option value="USER">USER</option>
            <option value="PARTNER">PARTNER</option>
          </select>
          <input data-testid="admin-identity-policy-duration" aria-label={t('adminIdentity.field.duration')} />
          <input data-testid="admin-identity-policy-forbidden-actions" aria-label={t('adminIdentity.field.forbiddenActions')} />
          <button data-testid="admin-identity-policy-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_IDENTITY_POLICY_SAVED')}>
            {t('adminIdentity.action.savePolicy')}
          </button>
        </section>
        <section>
          <input data-testid="admin-identity-impersonation-target" aria-label={t('adminIdentity.field.targetSubject')} />
          <select data-testid="admin-identity-impersonation-reason" aria-label={t('adminIdentity.field.reason')}>
            <option value="SUPPORT_DIAGNOSTICS">SUPPORT_DIAGNOSTICS</option>
          </select>
          <button data-testid="admin-identity-impersonation-start" onClick={() => {
            setImpersonating(true);
            setNotification('STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_STARTED');
          }}>
            {t('adminIdentity.action.startImpersonation')}
          </button>
          <button data-testid="admin-identity-forbidden-password-change" onClick={() => setNotification('STR_MNEMO_ADMIN_IDENTITY_IMPERSONATION_ACTION_FORBIDDEN')}>
            {t('adminIdentity.action.blockedPasswordChange')}
          </button>
        </section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'audit') {
    return (
      <main className="platform-page" data-testid="admin-identity-page">
        <h1>{t('adminIdentity.audit.title')}</h1>
        <input data-testid="admin-identity-audit-subject" aria-label={t('adminIdentity.field.subject')} />
        <button data-testid="admin-identity-audit-submit">{t('adminIdentity.action.search')}</button>
        <section data-testid="admin-identity-audit-table">
          <div>STATUS_CHANGED correlationId CORR-035-AUDIT</div>
          <div>IMPERSONATION_FORBIDDEN_ACTION correlationId CORR-035-IMP</div>
        </section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-identity-page">
      <h1>{t('adminIdentity.title')}</h1>
      <section data-testid="admin-identity-subject-table">
        <input data-testid="admin-identity-search" aria-label={t('adminIdentity.field.search')} />
        <select data-testid="admin-identity-subject-type" aria-label={t('adminIdentity.field.subjectType')}>
          <option value="USER">USER</option>
          <option value="PARTNER">PARTNER</option>
          <option value="EMPLOYEE">EMPLOYEE</option>
        </select>
        <select data-testid="admin-identity-status" aria-label={t('adminIdentity.field.status')}>
          <option value="ACTIVE">ACTIVE</option>
          <option value="SUSPENDED">SUSPENDED</option>
          <option value="BLOCKED">BLOCKED</option>
        </select>
        <button data-testid="admin-identity-search-submit">{t('adminIdentity.action.search')}</button>
        <div>
          <span>USR-035-1001 USER ACTIVE</span>
          <button data-testid="admin-identity-open-card-USR-035-1001" onClick={() => setCardOpen(true)}>
            {t('adminIdentity.action.open')}
          </button>
        </div>
      </section>
      {cardOpen ? (
        <section data-testid="admin-identity-subject-card">
          <h2>{t('adminIdentity.card.title')}</h2>
          <div data-testid="admin-identity-pii-mask">+7 *** ***-12-35</div>
          <section data-testid="admin-identity-eligibility-panel">PURCHASE ALLOWED OFFLINE_SALES_LIMIT</section>
          <section data-testid="admin-identity-audit-trail">STATUS_CHANGED correlationId</section>
          <button data-testid="admin-identity-change-status">{t('adminIdentity.action.changeStatus')}</button>
          <select data-testid="admin-identity-new-status" aria-label={t('adminIdentity.field.status')}>
            <option value="SUSPENDED">SUSPENDED</option>
            <option value="BLOCKED">BLOCKED</option>
          </select>
          <select data-testid="admin-identity-status-reason" aria-label={t('adminIdentity.field.reason')}>
            <option value="MANUAL_RISK_REVIEW">MANUAL_RISK_REVIEW</option>
          </select>
          <button data-testid="admin-identity-status-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_IDENTITY_STATUS_CHANGED')}>
            {t('adminIdentity.action.save')}
          </button>
        </section>
      ) : null}
      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
