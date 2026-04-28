import { useState } from 'react';
import { t } from '../i18n';

type AdminBonusSection = 'dashboard' | 'calculations' | 'payout-batches';

const ADMIN_BONUS_ROLES = new Set(['bonus-admin', 'mlm-manager', 'finance-manager', 'integration-admin', 'audit-admin', 'super-admin']);

export function AdminBonusView({ section = 'dashboard' }: { section?: AdminBonusSection }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [ruleStatus, setRuleStatus] = useState('DRAFT');
  const [calculationStatus, setCalculationStatus] = useState('');
  const [batchStatus, setBatchStatus] = useState('DRAFT');

  if (!ADMIN_BONUS_ROLES.has(role)) {
    return <main className="platform-page" data-testid="admin-bonus-forbidden">STR_MNEMO_ADMIN_BONUS_ACCESS_DENIED</main>;
  }

  if (section === 'calculations') {
    return (
      <main className="platform-page" data-testid="admin-bonus-program-page">
        <h1>{t('adminBonus.calculations.title')}</h1>
        <label>{t('adminBonus.field.period')}<input data-testid="bonus-calculation-period" defaultValue="2026-04" /></label>
        <button data-testid="bonus-calculation-run" onClick={() => setCalculationStatus('correlationId CORR-038-CALC PAYOUT_READY')}>
          {t('adminBonus.action.runCalculation')}
        </button>
        <section data-testid="bonus-calculation-status">{calculationStatus}</section>
      </main>
    );
  }

  if (section === 'payout-batches') {
    return (
      <main className="platform-page" data-testid="admin-bonus-program-page">
        <h1>{t('adminBonus.payout.title')}</h1>
        <label>{t('adminBonus.field.period')}<input data-testid="payout-batch-period" defaultValue="2026-04" /></label>
        <label>{t('adminBonus.field.currency')}<input data-testid="payout-batch-currency" defaultValue="RUB" /></label>
        <button data-testid="payout-batch-create" onClick={() => setBatchStatus('DRAFT')}>{t('adminBonus.action.createBatch')}</button>
        <button data-testid="payout-batch-approve" onClick={() => setBatchStatus('APPROVED')}>{t('adminBonus.action.approveBatch')}</button>
        <button data-testid="payout-batch-send" onClick={() => setBatchStatus('SENT')}>{t('adminBonus.action.sendBatch')}</button>
        <section data-testid="payout-batch-status">{batchStatus}</section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-bonus-program-page">
      <h1>{t('adminBonus.title')}</h1>
      <button data-testid="bonus-rule-create">{t('adminBonus.action.createRule')}</button>
      <label>{t('adminBonus.field.ruleCode')}<input data-testid="bonus-rule-code" /></label>
      <label>{t('adminBonus.field.ruleType')}<select data-testid="bonus-rule-type" defaultValue="ORDER_BONUS"><option value="ORDER_BONUS">ORDER_BONUS</option></select></label>
      <label>{t('adminBonus.field.currency')}<input data-testid="bonus-rule-currency" defaultValue="RUB" /></label>
      <label>{t('adminBonus.field.rate')}<input data-testid="bonus-rule-rate" defaultValue="7.5" /></label>
      <label>{t('adminBonus.field.priority')}<input data-testid="bonus-rule-priority" defaultValue="38" /></label>
      <button data-testid="bonus-rule-save" onClick={() => setRuleStatus('DRAFT')}>{t('adminBonus.action.saveRule')}</button>
      <button data-testid="bonus-rule-preview">{t('adminBonus.action.preview')}</button>
      <section data-testid="bonus-preview-result">expectedAmount 900.00 RUB</section>
      <button data-testid="bonus-rule-activate" onClick={() => setRuleStatus('ACTIVE')}>{t('adminBonus.action.activateRule')}</button>
      <section data-testid="bonus-rule-status">{ruleStatus}</section>
    </main>
  );
}
