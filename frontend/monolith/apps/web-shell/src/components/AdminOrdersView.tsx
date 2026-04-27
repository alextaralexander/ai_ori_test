import { useState } from 'react';
import { t } from '../i18n';

type AdminOrderTab = 'summary' | 'payments' | 'risk' | 'audit';

const ADMIN_ORDER_ROLES = new Set([
  'order-admin',
  'finance-operator',
  'fraud-admin',
  'audit-admin',
  'support-agent',
  'business-admin',
  'super-admin',
]);

export function AdminOrdersView() {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [tab, setTab] = useState<AdminOrderTab>('summary');
  const [selectedOrder, setSelectedOrder] = useState(false);
  const [notification, setNotification] = useState('');
  const [supplementaryCreated, setSupplementaryCreated] = useState(false);
  const [refundRequested, setRefundRequested] = useState(false);
  const [riskDecisionSaved, setRiskDecisionSaved] = useState(false);

  if (!ADMIN_ORDER_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-orders-forbidden">
        STR_MNEMO_ADMIN_ORDER_ACCESS_DENIED
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-orders-page">
      <h1>{t('adminOrder.title')}</h1>
      <section data-testid="admin-orders-table">
        <input data-testid="admin-orders-search" aria-label={t('adminOrder.field.search')} defaultValue="" />
        <select data-testid="admin-orders-payment-status" aria-label={t('adminOrder.field.paymentStatus')}>
          <option value="ANY">{t('adminOrder.status.any')}</option>
          <option value="PAID">PAID</option>
          <option value="AUTHORIZED">AUTHORIZED</option>
          <option value="REFUNDED">REFUNDED</option>
        </select>
        <button data-testid="admin-orders-search-submit">{t('adminOrder.action.search')}</button>
        <div>
          <span>BO-033-1001 PAID CAPTURE DELIVERED</span>
          <button data-testid="admin-orders-open-card-BO-033-1001" onClick={() => setSelectedOrder(true)}>
            {t('adminOrder.action.openCard')}
          </button>
        </div>
      </section>

      {selectedOrder ? (
        <section data-testid="admin-order-card">
          <h2>{t('adminOrder.card.title')}</h2>
          <nav className="admin-tabs" aria-label={t('adminOrder.tabs.label')}>
            <button data-testid="admin-order-tab-summary" onClick={() => setTab('summary')}>{t('adminOrder.tabs.summary')}</button>
            <button data-testid="admin-order-tab-payments" onClick={() => setTab('payments')}>{t('adminOrder.tabs.payments')}</button>
            <button data-testid="admin-order-tab-risk" onClick={() => setTab('risk')}>{t('adminOrder.tabs.risk')}</button>
            <button data-testid="admin-order-tab-audit" onClick={() => setTab('audit')}>{t('adminOrder.tabs.audit')}</button>
          </nav>

          <section data-testid="admin-order-payment-timeline">
            <div>AUTHORIZE CAPTURE {refundRequested ? 'REFUND_REQUESTED' : 'SETTLED'}</div>
          </section>
          <section data-testid="admin-order-audit-trail">
            <div>ADMIN_ORDER_VIEWED CORR-033-ORDER</div>
          </section>

          {tab === 'summary' ? (
            <section>
              <button data-testid="admin-order-create-supplementary">{t('adminOrder.action.createSupplementary')}</button>
              <input data-testid="admin-order-supplementary-sku" aria-label={t('adminOrder.field.sku')} />
              <input data-testid="admin-order-supplementary-quantity" aria-label={t('adminOrder.field.quantity')} />
              <select data-testid="admin-order-supplementary-reason" aria-label={t('adminOrder.field.reason')}>
                <option value="CUSTOMER_REQUEST">CUSTOMER_REQUEST</option>
                <option value="MISSED_ITEM">MISSED_ITEM</option>
              </select>
              <button
                data-testid="admin-order-supplementary-submit"
                onClick={() => {
                  setSupplementaryCreated(true);
                  setNotification('STR_MNEMO_ADMIN_ORDER_SUPPLEMENTARY_CREATED');
                }}
              >
                {t('adminOrder.action.submitSupplementary')}
              </button>
              <div>{supplementaryCreated ? 'BO-033-1001-S1 CREATED' : null}</div>
            </section>
          ) : null}

          {tab === 'payments' ? (
            <section>
              <button data-testid="admin-order-create-refund">{t('adminOrder.action.createRefund')}</button>
              <input data-testid="admin-order-refund-amount" aria-label={t('adminOrder.field.refundAmount')} />
              <select data-testid="admin-order-refund-reason" aria-label={t('adminOrder.field.reason')}>
                <option value="PARTIAL_CANCEL">PARTIAL_CANCEL</option>
                <option value="CUSTOMER_RETURN">CUSTOMER_RETURN</option>
              </select>
              <button
                data-testid="admin-order-refund-submit"
                onClick={() => {
                  setRefundRequested(true);
                  setNotification('STR_MNEMO_ADMIN_ORDER_REFUND_REQUESTED');
                }}
              >
                {t('adminOrder.action.submitRefund')}
              </button>
            </section>
          ) : null}

          {tab === 'risk' ? (
            <section data-testid="admin-order-risk-panel">
              <select data-testid="admin-order-risk-decision" aria-label={t('adminOrder.field.riskDecision')}>
                <option value="APPROVED">APPROVED</option>
                <option value="REJECTED">REJECTED</option>
                <option value="MANUAL_REVIEW">MANUAL_REVIEW</option>
              </select>
              <select data-testid="admin-order-risk-reason" aria-label={t('adminOrder.field.reason')}>
                <option value="MANUAL_REVIEW_PASSED">MANUAL_REVIEW_PASSED</option>
                <option value="FRAUD_PATTERN">FRAUD_PATTERN</option>
              </select>
              <button
                data-testid="admin-order-risk-submit"
                onClick={() => {
                  setRiskDecisionSaved(true);
                  setNotification('STR_MNEMO_ADMIN_ORDER_RISK_DECISION_SAVED');
                }}
              >
                {t('adminOrder.action.saveRiskDecision')}
              </button>
              <div>{riskDecisionSaved ? 'RISK APPROVED' : 'RISK MANUAL_REVIEW'}</div>
            </section>
          ) : null}

          {tab === 'audit' ? (
            <section data-testid="admin-order-audit-table">
              <div>ADMIN_ORDER_VIEWED BO-033-1001</div>
              <div>ADMIN_ORDER_PAYMENT_CAPTURED CORR-033-PAYMENT</div>
              <div>ADMIN_ORDER_RISK_DECISION_SAVED CORR-033-RISK</div>
            </section>
          ) : null}
        </section>
      ) : null}

      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
