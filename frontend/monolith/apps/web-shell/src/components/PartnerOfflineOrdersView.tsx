import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { executePartnerOfflineOrderAction, getPartnerOfflineOrder, OrderApiError, searchPartnerOfflineOrders, type PartnerOfflineOrderActionResponse, type PartnerOfflineOrderDetailsResponse, type PartnerOfflineOrderPageResponse, type PartnerOfflineOrderSummaryResponse } from '../api/order';
import { t } from '../i18n';

interface PartnerOfflineOrdersViewProps {
  orderNumber?: string;
  params?: URLSearchParams;
}

export function PartnerOfflineOrdersView({ orderNumber, params = new URLSearchParams() }: PartnerOfflineOrdersViewProps): ReactElement {
  const [orders, setOrders] = useState<PartnerOfflineOrderPageResponse | null>(null);
  const [details, setDetails] = useState<PartnerOfflineOrderDetailsResponse | null>(null);
  const [query, setQuery] = useState(params.get('query') ?? '');
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [actionResult, setActionResult] = useState<PartnerOfflineOrderActionResponse | null>(null);

  useEffect(() => {
    let active = true;
    const nextParams = new URLSearchParams(params);
    if (!nextParams.has('campaignId')) {
      nextParams.set('campaignId', 'CAT-2026-05');
    }
    setOrders(null);
    setDetails(null);
    setMessageCode(null);
    setActionResult(null);

    async function load(): Promise<void> {
      if (orderNumber) {
        const loaded = await getPartnerOfflineOrder(orderNumber);
        if (active) setDetails(loaded);
        return;
      }
      const loaded = await searchPartnerOfflineOrders(nextParams);
      if (active) setOrders(loaded);
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_PARTNER_OFFLINE_ORDER_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [orderNumber, params]);

  async function submitSearch(): Promise<void> {
    const next = new URLSearchParams(params);
    next.set('campaignId', next.get('campaignId') ?? 'CAT-2026-05');
    if (query) {
      next.set('query', query);
    } else {
      next.delete('query');
    }
    const loaded = await searchPartnerOfflineOrders(next);
    setOrders(loaded);
  }

  async function runAction(actionType: 'REPEAT_ORDER' | 'SERVICE_ADJUSTMENT'): Promise<void> {
    if (!details) return;
    const result = await executePartnerOfflineOrderAction(details.orderNumber, actionType);
    setActionResult(result);
    setMessageCode(result.resultMnemo);
  }

  if (messageCode === 'STR_MNEMO_PARTNER_OFFLINE_ORDER_ACCESS_DENIED') {
    return <Alert data-testid="partner-offline-orders-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (orderNumber) {
    const resultCode = actionResult?.resultMnemo ?? messageCode;
    return (
      <main className="partner-offline-page" data-testid="partner-offline-order-details-page">
        <Button href="/business/tools/order-management/vip-orders/partner-orders">{t('partnerOffline.back')}</Button>
        <header className="partner-offline-heading">
          <h1>{details?.orderNumber ?? orderNumber}</h1>
          <span>{details?.customerId} · {details?.partnerPersonNumber}</span>
        </header>
        {resultCode ? <Alert data-testid="partner-offline-order-action-result" message={`${t(resultCode)} (${resultCode})`} type="success" /> : null}
        <section className="partner-offline-details-grid">
          <article data-testid="partner-offline-order-details-items">
            <h2>{t('partnerOffline.items')}</h2>
            {details?.items.map((item) => <div key={item.sku}>{item.productName} x{item.quantity}</div>)}
          </article>
          <article>
            <h2>{t('partnerOffline.paymentDelivery')}</h2>
            <div>{details?.payment.paymentStatus} · {details?.delivery.deliveryTargetType}</div>
            <div>{details?.delivery.addressLine}</div>
            <div>{t('partnerOffline.businessVolume')}: {details?.businessVolume}</div>
            <div>{t('partnerOffline.bonusStatus')}: {details?.bonusAccrualStatus}</div>
          </article>
        </section>
        <section className="partner-offline-timeline" data-testid="partner-offline-order-details-timeline">
          <h2>{t('partnerOffline.timeline')}</h2>
          {details?.events.map((event) => <div key={`${event.eventType}-${event.occurredAt}`}>{event.descriptionMnemo ? `${t(event.descriptionMnemo)} (${event.descriptionMnemo})` : event.publicStatus}</div>)}
        </section>
        <Space wrap>
          <Button data-testid="partner-offline-order-repeat" onClick={() => void runAction('REPEAT_ORDER')} type="primary">{t('partnerOffline.repeat')}</Button>
          <Button data-testid="partner-offline-order-adjustment" onClick={() => void runAction('SERVICE_ADJUSTMENT')}>{t('partnerOffline.adjustment')}</Button>
          {details?.linkedEntities.partnerCardPath ? <Button href={details.linkedEntities.partnerCardPath}>{t('partnerOffline.partnerCard')}</Button> : null}
        </Space>
      </main>
    );
  }

  return (
    <main className="partner-offline-page" data-testid="partner-offline-orders-page">
      <header className="partner-offline-heading">
        <h1>{t('partnerOffline.title')}</h1>
        <span>{t('partnerOffline.description')}</span>
      </header>
      {orders?.messageMnemo ? <Alert message={`${t(orders.messageMnemo)} (${orders.messageMnemo})`} type="info" /> : null}
      <section className="partner-offline-filters" data-testid="partner-offline-orders-filters">
        <Input data-testid="partner-offline-orders-search" onChange={(event) => setQuery(event.target.value)} placeholder={t('partnerOffline.search')} value={query} />
        <Button data-testid="partner-offline-orders-submit" onClick={() => void submitSearch()} type="primary">{t('partnerOffline.submit')}</Button>
      </section>
      <section className="partner-offline-list">
        {orders?.items.map((order) => <PartnerOfflineOrderCard key={order.orderNumber} order={order} />)}
      </section>
    </main>
  );
}

function PartnerOfflineOrderCard({ order }: { order: PartnerOfflineOrderSummaryResponse }): ReactElement {
  return (
    <article className="partner-offline-card" data-testid={`partner-offline-order-card-${order.orderNumber}`}>
      <h2>{order.orderNumber}</h2>
      <span>{order.customerId} · {order.customerSegment}</span>
      <span>{order.partnerPersonNumber}</span>
      <span>{order.paymentStatus} · {order.deliveryStatus}</span>
      <strong>{order.grandTotalAmount} {order.currencyCode}</strong>
      <span>{order.bonusAccrualStatus}</span>
      <a href={`/business/tools/order-management/vip-orders/partner-orders/${order.orderNumber}`}>{t('partnerOffline.details')}</a>
    </article>
  );
}
