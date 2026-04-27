import { useState } from 'react';
import { t } from '../i18n';

type AdminServiceTab = 'summary' | 'decisions' | 'replacement' | 'audit';

const ADMIN_SERVICE_ROLES = new Set([
  'claim-operator',
  'service-supervisor',
  'audit-admin',
  'business-admin',
  'super-admin',
]);

export function AdminServiceView({ section = 'queue' }: { section?: 'queue' | 'sla-board' }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [tab, setTab] = useState<AdminServiceTab>('summary');
  const [selectedCase, setSelectedCase] = useState(false);
  const [notification, setNotification] = useState('');
  const [assigned, setAssigned] = useState(false);
  const [refundRequested, setRefundRequested] = useState(false);
  const [replacementRequested, setReplacementRequested] = useState(false);

  if (!ADMIN_SERVICE_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-service-forbidden">
        STR_MNEMO_ADMIN_SERVICE_ACCESS_DENIED
      </main>
    );
  }

  if (section === 'sla-board') {
    return (
      <main className="platform-page" data-testid="admin-service-sla-board">
        <h1>{t('adminService.sla.title')}</h1>
        <section>
          <div data-testid="admin-service-sla-active-count">{t('adminService.sla.active')}: 184</div>
          <div data-testid="admin-service-sla-at-risk-count">{t('adminService.sla.atRisk')}: 27</div>
          <div data-testid="admin-service-sla-breached-count">{t('adminService.sla.breached')}: 9</div>
        </section>
        <section data-testid="admin-service-sla-escalations">
          <div>
            <span>CLM-034-1007 BREACHED DAMAGED_ITEM</span>
            <button data-testid="admin-service-open-escalation-CLM-034-1007">{t('adminService.action.open')}</button>
          </div>
          <button data-testid="admin-service-supervisor-override">{t('adminService.action.override')}</button>
          <select data-testid="admin-service-supervisor-override-reason" aria-label={t('adminService.field.reason')}>
            <option value="SUPERVISOR_GOODWILL">SUPERVISOR_GOODWILL</option>
            <option value="SLA_BREACH_COMPENSATION">SLA_BREACH_COMPENSATION</option>
          </select>
          <button data-testid="admin-service-supervisor-override-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_SERVICE_OVERRIDE_SAVED')}>
            {t('adminService.action.saveOverride')}
          </button>
        </section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-service-page">
      <h1>{t('adminService.title')}</h1>
      <section data-testid="admin-service-case-table">
        <input data-testid="admin-service-search" aria-label={t('adminService.field.search')} defaultValue="" />
        <select data-testid="admin-service-sla-status" aria-label={t('adminService.field.slaStatus')}>
          <option value="ANY">{t('adminService.status.any')}</option>
          <option value="ON_TRACK">ON_TRACK</option>
          <option value="AT_RISK">AT_RISK</option>
          <option value="BREACHED">BREACHED</option>
          <option value="PAUSED">PAUSED</option>
        </select>
        <select data-testid="admin-service-claim-type" aria-label={t('adminService.field.claimType')}>
          <option value="DAMAGED_ITEM">DAMAGED_ITEM</option>
          <option value="WRONG_ITEM">WRONG_ITEM</option>
          <option value="DELIVERY_LOST">DELIVERY_LOST</option>
        </select>
        <button data-testid="admin-service-search-submit">{t('adminService.action.search')}</button>
        <div>
          <span>CLM-034-1001 DAMAGED_ITEM AT_RISK NEW</span>
          <button data-testid="admin-service-open-card-CLM-034-1001" onClick={() => setSelectedCase(true)}>
            {t('adminService.action.open')}
          </button>
        </div>
      </section>

      {selectedCase ? (
        <section data-testid="admin-service-case-card">
          <h2>{t('adminService.card.title')}</h2>
          <div data-testid="admin-service-case-sla">AT_RISK</div>
          <nav className="admin-tabs" aria-label={t('adminService.tabs.label')}>
            <button data-testid="admin-service-tab-summary" onClick={() => setTab('summary')}>{t('adminService.tabs.summary')}</button>
            <button data-testid="admin-service-tab-decisions" onClick={() => setTab('decisions')}>{t('adminService.tabs.decisions')}</button>
            <button data-testid="admin-service-tab-replacement" onClick={() => setTab('replacement')}>{t('adminService.tabs.replacement')}</button>
            <button data-testid="admin-service-tab-audit" onClick={() => setTab('audit')}>{t('adminService.tabs.audit')}</button>
          </nav>
          <section data-testid="admin-service-audit-trail">
            <div>ADMIN_SERVICE_CASE_VIEWED CORR-034-CASE</div>
          </section>

          {tab === 'summary' ? (
            <section>
              <button data-testid="admin-service-take-case" onClick={() => {
                setAssigned(true);
                setNotification('STR_MNEMO_ADMIN_SERVICE_CASE_ASSIGNED');
              }}>
                {t('adminService.action.takeCase')}
              </button>
              <button data-testid="admin-service-request-info">{t('adminService.action.requestInfo')}</button>
              <select data-testid="admin-service-request-info-message-code" aria-label={t('adminService.field.messageCode')}>
                <option value="STR_MNEMO_ADMIN_SERVICE_REQUEST_PHOTO">STR_MNEMO_ADMIN_SERVICE_REQUEST_PHOTO</option>
              </select>
              <select data-testid="admin-service-request-info-reason" aria-label={t('adminService.field.reason')}>
                <option value="WAITING_CUSTOMER_DATA">WAITING_CUSTOMER_DATA</option>
              </select>
              <button data-testid="admin-service-request-info-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_SERVICE_REQUEST_SENT')}>
                {t('adminService.action.sendRequest')}
              </button>
              <div>{assigned ? 'IN_PROGRESS' : 'NEW'}</div>
            </section>
          ) : null}

          {tab === 'decisions' ? (
            <section>
              <select data-testid="admin-service-decision-type" aria-label={t('adminService.field.decisionType')}>
                <option value="APPROVE_REFUND">APPROVE_REFUND</option>
                <option value="APPROVE_REPLACEMENT">APPROVE_REPLACEMENT</option>
                <option value="REJECT">REJECT</option>
              </select>
              <select data-testid="admin-service-decision-reason" aria-label={t('adminService.field.reason')}>
                <option value="DAMAGED_ITEM_CONFIRMED">DAMAGED_ITEM_CONFIRMED</option>
              </select>
              <select data-testid="admin-service-decision-message-code" aria-label={t('adminService.field.messageCode')}>
                <option value="STR_MNEMO_ADMIN_SERVICE_REFUND_APPROVED">STR_MNEMO_ADMIN_SERVICE_REFUND_APPROVED</option>
              </select>
              <button data-testid="admin-service-decision-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_SERVICE_DECISION_SAVED')}>
                {t('adminService.action.saveDecision')}
              </button>
              <input data-testid="admin-service-refund-amount" aria-label={t('adminService.field.refundAmount')} />
              <button data-testid="admin-service-refund-submit" onClick={() => {
                setRefundRequested(true);
                setNotification('STR_MNEMO_ADMIN_SERVICE_REFUND_REQUESTED');
              }}>
                {t('adminService.action.requestRefund')}
              </button>
              <div>{refundRequested ? 'REFUND_REQUESTED' : null}</div>
            </section>
          ) : null}

          {tab === 'replacement' ? (
            <section>
              <input data-testid="admin-service-replacement-sku" aria-label={t('adminService.field.sku')} />
              <input data-testid="admin-service-replacement-quantity" aria-label={t('adminService.field.quantity')} />
              <select data-testid="admin-service-replacement-warehouse" aria-label={t('adminService.field.warehouse')}>
                <option value="WH-MSK-01">WH-MSK-01</option>
              </select>
              <button data-testid="admin-service-replacement-submit" onClick={() => {
                setReplacementRequested(true);
                setNotification('STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_REQUESTED');
              }}>
                {t('adminService.action.requestReplacement')}
              </button>
              <div>{replacementRequested ? 'REPLACEMENT_REQUESTED' : null}</div>
            </section>
          ) : null}

          {tab === 'audit' ? (
            <section data-testid="admin-service-audit-table">
              <div>ADMIN_SERVICE_DECISION_SAVED CLM-034-1001</div>
              <div>ADMIN_SERVICE_REFUND_REQUESTED CORR-034-REFUND</div>
            </section>
          ) : null}
        </section>
      ) : null}

      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
