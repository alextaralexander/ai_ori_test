import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { getPartnerOfficeReport, getPartnerOfficeSupply, getPartnerOfficeSupplyOrder, PartnerOfficeApiError, recordPartnerOfficeDeviation, searchPartnerOfficeOrders, searchPartnerOfficeSupply, transitionPartnerOfficeSupply, type PartnerOfficeActionResponse, type PartnerOfficeOrderPageResponse, type PartnerOfficeOrderSummaryResponse, type PartnerOfficeReportResponse, type PartnerOfficeSupplyDetailsResponse, type PartnerOfficeSupplyOrderDetailsResponse, type PartnerOfficeSupplyPageResponse, type PartnerOfficeSupplySummaryResponse } from '../api/partnerOffice';
import { t } from '../i18n';

interface PartnerOfficeViewProps {
  mode: 'orders' | 'supply' | 'supply-details' | 'supply-order' | 'report';
  params?: URLSearchParams;
  orderNumber?: string;
  supplyId?: string;
}

export function PartnerOfficeView({ mode, params = new URLSearchParams(), orderNumber, supplyId }: PartnerOfficeViewProps): ReactElement {
  const [orders, setOrders] = useState<PartnerOfficeOrderPageResponse | null>(null);
  const [supplyPage, setSupplyPage] = useState<PartnerOfficeSupplyPageResponse | null>(null);
  const [supplyDetails, setSupplyDetails] = useState<PartnerOfficeSupplyDetailsResponse | null>(null);
  const [orderDetails, setOrderDetails] = useState<PartnerOfficeSupplyOrderDetailsResponse | null>(null);
  const [report, setReport] = useState<PartnerOfficeReportResponse | null>(null);
  const [query, setQuery] = useState(params.get('query') ?? '');
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [actionResult, setActionResult] = useState<PartnerOfficeActionResponse | null>(null);

  useEffect(() => {
    let active = true;
    setMessageCode(null);
    setActionResult(null);

    async function load(): Promise<void> {
      if (mode === 'orders') {
        const loaded = await searchPartnerOfficeOrders(defaultParams(params));
        if (active) setOrders(loaded);
      } else if (mode === 'supply') {
        const loaded = await searchPartnerOfficeSupply(defaultParams(params));
        if (active) setSupplyPage(loaded);
      } else if (mode === 'supply-details' && supplyId) {
        const loaded = await getPartnerOfficeSupply(supplyId);
        if (active) setSupplyDetails(loaded);
      } else if (mode === 'supply-order' && orderNumber) {
        const loaded = await getPartnerOfficeSupplyOrder(orderNumber);
        if (active) setOrderDetails(loaded);
      } else if (mode === 'report') {
        const loaded = await getPartnerOfficeReport(defaultParams(params));
        if (active) setReport(loaded);
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof PartnerOfficeApiError ? error.code : 'STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [mode, orderNumber, params, supplyId]);

  async function submitOrdersSearch(): Promise<void> {
    const next = defaultParams(params);
    if (query) {
      next.set('query', query);
    } else {
      next.delete('query');
    }
    setOrders(await searchPartnerOfficeOrders(next));
  }

  async function runTransition(): Promise<void> {
    if (!supplyDetails) return;
    const result = await transitionPartnerOfficeSupply(supplyDetails.supply.supplyId, 'ARRIVED');
    setActionResult(result);
    setMessageCode(result.messageCode);
  }

  async function runDeviation(): Promise<void> {
    if (!orderDetails) return;
    const result = await recordPartnerOfficeDeviation(orderDetails.order.orderNumber, orderDetails.order.supplyId);
    setActionResult(result);
    setMessageCode(result.messageCode);
  }

  if (messageCode === 'STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED') {
    return <Alert data-testid="partner-office-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (mode === 'orders') {
    return (
      <main className="partner-office-page" data-testid="partner-office-orders-page">
        <PageHeader titleKey="partnerOffice.orders.title" subtitleKey="partnerOffice.orders.description" />
        <section className="partner-office-filters" data-testid="partner-office-orders-filters">
          <Input data-testid="partner-office-orders-search" onChange={(event) => setQuery(event.target.value)} placeholder={t('partnerOffice.search')} value={query} />
          <Button data-testid="partner-office-orders-submit" onClick={() => void submitOrdersSearch()} type="primary">{t('partnerOffice.submit')}</Button>
        </section>
        <section className="partner-office-list">
          {orders?.items.map((order) => <OrderCard key={order.orderNumber} order={order} />)}
        </section>
      </main>
    );
  }

  if (mode === 'supply') {
    return (
      <main className="partner-office-page" data-testid="partner-office-supply-page">
        <PageHeader titleKey="partnerOffice.supply.title" subtitleKey="partnerOffice.supply.description" />
        <section className="partner-office-list">
          {supplyPage?.items.map((supply) => <SupplyCard key={supply.supplyId} supply={supply} />)}
        </section>
      </main>
    );
  }

  if (mode === 'supply-details') {
    const resultCode = actionResult?.messageCode ?? messageCode;
    return (
      <main className="partner-office-page" data-testid="partner-office-supply-details-page">
        <Button href="/partner-office/supply">{t('partnerOffice.backToSupply')}</Button>
        <PageHeader title={supplyDetails?.supply.supplyId ?? supplyId} subtitle={supplyDetails?.supply.status} />
        {resultCode ? <Alert data-testid="partner-office-action-result" message={`${t(resultCode)} (${resultCode})`} type="success" /> : null}
        <section className="partner-office-details-grid">
          <article data-testid="partner-office-supply-orders">
            <h2>{t('partnerOffice.orders')}</h2>
            {supplyDetails?.orders.map((order) => <OrderCard key={order.orderNumber} order={order} compact />)}
          </article>
          <article data-testid="partner-office-supply-movements">
            <h2>{t('partnerOffice.movements')}</h2>
            {supplyDetails?.movements.map((movement) => <div key={`${movement.movementType}-${movement.occurredAt}`}>{movement.movementType} · {movement.sourceSystem}</div>)}
          </article>
        </section>
        <section className="partner-office-details-grid">
          <article>
            <h2>{t('partnerOffice.deviations')}</h2>
            {supplyDetails?.deviations.map((deviation) => <div key={deviation.deviationId}>{deviation.deviationType} · {deviation.sku} · {deviation.reasonCode}</div>)}
          </article>
          <article data-testid="partner-office-supply-actions">
            <h2>{t('partnerOffice.actions')}</h2>
            <Button onClick={() => void runTransition()} type="primary">{t('partnerOffice.transitionArrived')}</Button>
          </article>
        </section>
      </main>
    );
  }

  if (mode === 'supply-order') {
    const resultCode = actionResult?.messageCode ?? messageCode;
    return (
      <main className="partner-office-page" data-testid="partner-office-supply-order-details-page">
        <Button href="/partner-office/supply">{t('partnerOffice.backToSupply')}</Button>
        <PageHeader title={orderDetails?.order.orderNumber ?? orderNumber} subtitle={orderDetails?.order.supplyId} />
        {resultCode ? <Alert data-testid="partner-office-action-result" message={`${t(resultCode)} (${resultCode})`} type="success" /> : null}
        <section className="partner-office-details-grid">
          <article data-testid="partner-office-supply-order-items">
            <h2>{t('partnerOffice.items')}</h2>
            {orderDetails?.items.map((item) => <div key={`${item.sku}-${item.boxNumber}`}>{item.productName} · {item.sku} · {item.acceptedQuantity}/{item.expectedQuantity}</div>)}
          </article>
          <article>
            <h2>{t('partnerOffice.workflowLinks')}</h2>
            {Object.entries(orderDetails?.workflowLinks ?? {}).map(([key, value]) => <a href={value} key={key}>{key}</a>)}
          </article>
        </section>
        <Space wrap>
          <Button data-testid="partner-office-record-deviation" onClick={() => void runDeviation()} type="primary">{t('partnerOffice.recordDeviation')}</Button>
        </Space>
      </main>
    );
  }

  return (
    <main className="partner-office-page" data-testid="partner-office-report-page">
      <PageHeader titleKey="partnerOffice.report.title" subtitleKey="partnerOffice.report.description" />
      <section className="partner-office-kpi" data-testid="partner-office-report-kpi">
        <Metric labelKey="partnerOffice.report.supplyCount" value={report?.supplyCount} />
        <Metric labelKey="partnerOffice.report.orderCount" value={report?.orderCount} />
        <Metric labelKey="partnerOffice.report.shortageCount" value={report?.shortageCount} />
        <Metric labelKey="partnerOffice.report.acceptanceSla" value={report?.acceptanceSlaPercent} />
      </section>
      <section className="partner-office-list">
        {report?.escalations.map((escalation) => (
          <article className="partner-office-card" key={`${escalation.supplyId}-${escalation.reasonCode}`}>
            <h2>{escalation.supplyId}</h2>
            <span>{escalation.reasonCode}</span>
            <span>{escalation.ownerUserId}</span>
            <span>{escalation.status}</span>
          </article>
        ))}
      </section>
    </main>
  );
}

function PageHeader({ title, titleKey, subtitle, subtitleKey }: { title?: string | null; titleKey?: string; subtitle?: string | null; subtitleKey?: string }): ReactElement {
  return (
    <header className="partner-office-heading">
      <h1>{titleKey ? t(titleKey) : title}</h1>
      <span>{subtitleKey ? t(subtitleKey) : subtitle}</span>
    </header>
  );
}

function OrderCard({ order, compact = false }: { order: PartnerOfficeOrderSummaryResponse; compact?: boolean }): ReactElement {
  return (
    <article className="partner-office-card" data-testid={`partner-office-order-card-${order.orderNumber}`}>
      <h2>{order.orderNumber}</h2>
      <span>{order.customerId} · {order.partnerPersonNumber}</span>
      {!compact ? <span>{order.campaignId}</span> : null}
      <span>{order.supplyId}</span>
      <span>{order.paymentStatus} · {order.deliveryStatus}</span>
      <strong>{order.grandTotalAmount} {order.currency}</strong>
      <span>{order.hasDeviation ? t('partnerOffice.hasDeviation') : t('partnerOffice.noDeviation')}</span>
      <a href={`/partner-office/supply/orders/${order.orderNumber}`}>{t('partnerOffice.details')}</a>
    </article>
  );
}

function SupplyCard({ supply }: { supply: PartnerOfficeSupplySummaryResponse }): ReactElement {
  return (
    <article className="partner-office-card" data-testid={`partner-office-supply-card-${supply.supplyId}`}>
      <h2>{supply.supplyId}</h2>
      <span>{supply.officeId} · {supply.regionId}</span>
      <span>{supply.status}</span>
      <span>{supply.externalWmsDocumentId}</span>
      <span>{supply.orderCount} / {supply.boxCount} / {supply.skuCount}</span>
      <strong>{supply.deviationCount}</strong>
      <a href={`/partner-office/supply/${supply.supplyId}`}>{t('partnerOffice.details')}</a>
    </article>
  );
}

function Metric({ labelKey, value }: { labelKey: string; value?: number | string }): ReactElement {
  return (
    <article className="partner-office-metric">
      <span>{t(labelKey)}</span>
      <strong>{value ?? '-'}</strong>
    </article>
  );
}

function defaultParams(params: URLSearchParams): URLSearchParams {
  const next = new URLSearchParams(params);
  if (!next.has('campaignId')) {
    next.set('campaignId', 'CAT-2026-05');
  }
  return next;
}
