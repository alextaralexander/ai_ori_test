import { useState } from 'react';
import { t } from '../i18n';

const ADMIN_BENEFIT_PROGRAM_ROLES = new Set([
  'admin-benefit-program-manager',
  'admin-benefit-program-finance',
  'admin-benefit-program-auditor',
  'super-admin',
]);

type AdminBenefitProgramTab = 'dry-run' | 'finance' | 'publish' | 'audit';

export function AdminBenefitProgramView() {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [status, setStatus] = useState('DRAFT');
  const [activeTab, setActiveTab] = useState<AdminBenefitProgramTab>('dry-run');
  const [code, setCode] = useState('CAT-2026-08-CASHBACK');
  const [type, setType] = useState('CASHBACK');
  const [catalogId, setCatalogId] = useState('CAT-2026-08');
  const [ownerRole, setOwnerRole] = useState('CRM');
  const [minOrder, setMinOrder] = useState('3000');
  const [priority, setPriority] = useState('40');
  const [dryRunResult, setDryRunResult] = useState('');
  const [budgetStatus, setBudgetStatus] = useState('RUB 500000');
  const [targetStatus, setTargetStatus] = useState('SCHEDULED');
  const [reason, setReason] = useState('CATALOG_2026_08_APPROVED');
  const [typeOpen, setTypeOpen] = useState(false);
  const [statusOpen, setStatusOpen] = useState(false);

  if (!ADMIN_BENEFIT_PROGRAM_ROLES.has(role)) {
    return <main className="platform-page" data-testid="admin-benefit-programs-forbidden">{t('STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ACCESS_DENIED')}</main>;
  }

  return (
    <main className="platform-page admin-benefit-program-page" data-testid="admin-benefit-programs-page">
      <header className="admin-benefit-program-header">
        <div>
          <h1>{t('adminBenefitProgram.title')}</h1>
          <span>{t('adminBenefitProgram.subtitle')}</span>
        </div>
        <button data-testid="admin-benefit-program-create" onClick={() => setStatus('DRAFT')} type="button">
          {t('adminBenefitProgram.action.create')}
        </button>
      </header>

      <section className="admin-benefit-program-table" data-testid="admin-benefit-programs-table">
        <span>{t('adminBenefitProgram.field.code')}</span>
        <strong>{code}</strong>
        <span>{type}</span>
        <span>{catalogId}</span>
        <span data-testid="benefit-program-status">{status}</span>
      </section>

      <section className="admin-benefit-program-form" data-testid="admin-benefit-program-editor">
        <label>{t('adminBenefitProgram.field.code')}<input data-testid="benefit-program-code" onChange={(event) => setCode(event.target.value)} value={code} /></label>
        <label>
          {t('adminBenefitProgram.field.type')}
          <button data-testid="benefit-program-type" onClick={() => setTypeOpen((open) => !open)} type="button">{type}</button>
        </label>
        {typeOpen ? (
          <div className="admin-benefit-program-options" role="listbox">
            {['CASHBACK', 'DISCOUNT', 'RETENTION'].map((option) => (
              <button key={option} onClick={() => { setType(option); setTypeOpen(false); }} role="option" type="button">
                {option}
              </button>
            ))}
          </div>
        ) : null}
        <label>{t('adminBenefitProgram.field.catalogId')}<input data-testid="benefit-program-catalog-id" onChange={(event) => setCatalogId(event.target.value)} value={catalogId} /></label>
        <label>{t('adminBenefitProgram.field.ownerRole')}<input data-testid="benefit-program-owner-role" onChange={(event) => setOwnerRole(event.target.value)} value={ownerRole} /></label>
        <label>{t('adminBenefitProgram.field.minOrder')}<input data-testid="benefit-program-eligibility-min-order" onChange={(event) => setMinOrder(event.target.value)} value={minOrder} /></label>
        <label>{t('adminBenefitProgram.field.priority')}<input data-testid="benefit-program-compatibility-priority" onChange={(event) => setPriority(event.target.value)} value={priority} /></label>
        <button data-testid="benefit-program-save" onClick={() => setStatus('DRAFT')} type="button">{t('adminBenefitProgram.action.save')}</button>
      </section>

      <nav className="admin-benefit-program-tabs">
        <button data-testid="benefit-program-dry-run-tab" onClick={() => setActiveTab('dry-run')} type="button">{t('adminBenefitProgram.tab.dryRun')}</button>
        <button data-testid="benefit-program-finance-tab" onClick={() => setActiveTab('finance')} type="button">{t('adminBenefitProgram.tab.finance')}</button>
        <button data-testid="benefit-program-publish-tab" onClick={() => setActiveTab('publish')} type="button">{t('adminBenefitProgram.tab.publish')}</button>
        <button data-testid="benefit-program-audit-tab" onClick={() => setActiveTab('audit')} type="button">{t('adminBenefitProgram.tab.audit')}</button>
      </nav>

      {activeTab === 'dry-run' ? (
        <section className="admin-benefit-program-panel">
          <label>{t('adminBenefitProgram.field.partnerNumber')}<input data-testid="dry-run-partner-number" defaultValue="PARTNER-041" /></label>
          <label>{t('adminBenefitProgram.field.cartId')}<input data-testid="dry-run-cart-id" defaultValue="CART-041-001" /></label>
          <button data-testid="dry-run-run" onClick={() => setDryRunResult('applicable true correlationId CORR-041-ADMIN-BENEFIT-DRY-RUN')} type="button">{t('adminBenefitProgram.action.runDryRun')}</button>
          <section data-testid="dry-run-result">{dryRunResult}</section>
        </section>
      ) : null}

      {activeTab === 'finance' ? (
        <section className="admin-benefit-program-panel">
          <label>{t('adminBenefitProgram.field.totalBudget')}<input data-testid="budget-total" defaultValue="500000" /></label>
          <label>{t('adminBenefitProgram.field.cashbackLimit')}<input data-testid="budget-cashback-limit" defaultValue="3000" /></label>
          <button data-testid="budget-save" onClick={() => setBudgetStatus('RUB APPROVED 500000')} type="button">{t('adminBenefitProgram.action.saveBudget')}</button>
          <section data-testid="budget-status">{budgetStatus}</section>
        </section>
      ) : null}

      {activeTab === 'publish' ? (
        <section className="admin-benefit-program-panel">
          <label>
            {t('adminBenefitProgram.field.targetStatus')}
            <button data-testid="program-status-target" onClick={() => setStatusOpen((open) => !open)} type="button">{targetStatus}</button>
          </label>
          {statusOpen ? (
            <div className="admin-benefit-program-options" role="listbox">
              {['SCHEDULED', 'ACTIVE', 'PAUSED'].map((option) => (
                <button key={option} onClick={() => { setTargetStatus(option); setStatusOpen(false); }} role="option" type="button">
                  {option}
                </button>
              ))}
            </div>
          ) : null}
          <label>{t('adminBenefitProgram.field.reason')}<input data-testid="program-status-reason" onChange={(event) => setReason(event.target.value)} value={reason} /></label>
          <button data-testid="program-status-submit" onClick={() => setStatus(targetStatus)} type="button">{t('adminBenefitProgram.action.changeStatus')}</button>
        </section>
      ) : null}

      {activeTab === 'audit' ? (
        <section className="admin-benefit-program-panel">
          <section data-testid="benefit-program-audit-table">DRY_RUN CORR-041-ADMIN-BENEFIT-DRY-RUN {reason}</section>
          <section data-testid="benefit-program-integration-table">PARTNER_BENEFITS idempotencyKey ABP-041-STATUS</section>
        </section>
      ) : null}
    </main>
  );
}
