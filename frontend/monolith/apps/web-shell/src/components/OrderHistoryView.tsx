import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { getOrderDetails, OrderApiError, repeatOrder, searchOrderHistory, type OrderDetailsResponse, type OrderHistoryItemResponse, type OrderHistoryPageResponse, type RepeatOrderResponse } from '../api/order';
import { t } from '../i18n';

interface OrderHistoryViewProps {
  orderNumber?: string | null;
  params?: URLSearchParams;
}

export function OrderHistoryView({ orderNumber, params = new URLSearchParams() }: OrderHistoryViewProps): ReactElement {
  const [history, setHistory] = useState<OrderHistoryPageResponse | null>(null);
  const [details, setDetails] = useState<OrderDetailsResponse | null>(null);
  const [query, setQuery] = useState(params.get('query') ?? '');
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [paymentActionResultCode, setPaymentActionResultCode] = useState<string | null>(null);
  const [repeatResult, setRepeatResult] = useState<RepeatOrderResponse | null>(null);
  const mobileFiltersVisible = useMemo(() => window.innerWidth <= 640, []);

  useEffect(() => {
    let active = true;
    async function load(): Promise<void> {
      setMessageCode(null);
      setPaymentActionResultCode(null);
      setRepeatResult(null);
      if (orderNumber) {
        const loaded = await getOrderDetails(orderNumber);
        if (active) {
          setDetails(loaded);
        }
        return;
      }
      const loaded = await searchOrderHistory(params);
      if (active) {
        setHistory(loaded);
      }
    }
    void load().catch((error: unknown) => {
      setMessageCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_ORDER_HISTORY_EMPTY');
    });
    return () => {
      active = false;
    };
  }, [orderNumber, params]);

  async function submitSearch(): Promise<void> {
    const next = new URLSearchParams(params);
    if (query) {
      next.set('query', query);
    } else {
      next.delete('query');
    }
    const loaded = await searchOrderHistory(next);
    setHistory(loaded);
  }

  async function handleRepeat(targetOrderNumber: string): Promise<void> {
    const result = await repeatOrder(targetOrderNumber);
    setRepeatResult(result);
    setMessageCode(result.reasonMnemo ?? 'STR_MNEMO_CART_ITEM_ADDED');
  }

  if (messageCode === 'STR_MNEMO_ORDER_HISTORY_ACCESS_DENIED') {
    return <Alert data-testid="order-history-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (orderNumber) {
    const warningCode = messageCode ?? details?.warnings[0]?.code ?? null;

    return (
      <main className="order-history-page" data-testid="order-details-page">
        {warningCode ? <Alert data-testid="order-payment-warning" message={`${t(warningCode)} (${warningCode})`} type="warning" /> : null}
        <Button href="/order/order-history">{t('order.history.back')}</Button>
        <header className="order-history-heading">
          <h1>{details?.orderNumber ?? orderNumber}</h1>
          <span>{details?.orderType}</span>
        </header>
        {details?.businessVolume ? <section data-testid="order-details-partner-benefits">{t('order.history.partnerBenefits')}: {details.businessVolume}</section> : null}
        <section className="order-details-grid">
          <article data-testid="order-details-items">
            <h2>{t('order.history.items')}</h2>
            {details?.items.map((item) => <div key={item.sku}>{item.productName} x{item.quantity}</div>)}
          </article>
          <article>
            <h2>{t('order.history.deliveryAndPayment')}</h2>
            <div>{details?.delivery.addressLine}</div>
            <div>{details?.payment.paymentStatus}</div>
          </article>
        </section>
        <section data-testid="order-details-timeline">
          <h2>{t('order.history.timeline')}</h2>
          {details?.events.map((event) => <div key={`${event.eventType}-${event.occurredAt}`}>{event.descriptionMnemo ? t(event.descriptionMnemo) : event.publicStatus}</div>)}
        </section>
        <Space wrap>
          {details?.actions.paymentAvailable ? <Button data-testid="order-details-pay" onClick={() => {
            setMessageCode('STR_MNEMO_ORDER_PAYMENT_PENDING');
            setPaymentActionResultCode('STR_MNEMO_ORDER_PAYMENT_PENDING');
          }}>{t('order.history.pay')}</Button> : null}
          {details?.actions.repeatOrderAvailable ? <Button data-testid={details.orderType === 'SUPPLEMENTARY' ? 'order-details-repeat-supplementary' : 'order-details-repeat'} onClick={() => void handleRepeat(details.orderNumber)}>{t('order.history.repeat')}</Button> : null}
          {details?.actions.claimAvailable ? <Button>{t('order.history.claim')}</Button> : null}
        </Space>
        {paymentActionResultCode ? <Alert data-testid="order-payment-action-result" message={`${t(paymentActionResultCode)} (${paymentActionResultCode})`} type="success" /> : null}
        {repeatResult ? <Alert data-testid="order-payment-action-result" message={`${repeatResult.status}: ${repeatResult.addedItems.length}`} type="success" /> : null}
      </main>
    );
  }

  return (
    <main className="order-history-page" data-testid="order-history-page">
      <header className="order-history-heading">
        <h1>{t('order.history.title')}</h1>
        <span>{t('order.history.description')}</span>
      </header>
      <section className="order-history-filters" data-testid={mobileFiltersVisible ? 'order-history-mobile-filters' : 'order-history-filters'}>
        <Input data-testid="order-history-search" onChange={(event) => setQuery(event.target.value)} placeholder={t('order.history.search')} value={query} />
        <Button data-testid="order-history-submit" onClick={() => void submitSearch()} type="primary">{t('order.history.submit')}</Button>
      </section>
      <section className="order-history-list">
        {history?.items.map((order) => <OrderCard key={order.orderNumber} order={order} />)}
      </section>
    </main>
  );
}

function OrderCard({ order }: { order: OrderHistoryItemResponse }): ReactElement {
  return (
    <article className="order-history-card" data-testid={`order-history-card-${order.orderNumber}`}>
      <h2>{order.orderNumber}</h2>
      <span>{order.orderType} · {order.campaignId}</span>
      <span>{order.paymentStatus} · {order.deliveryStatus}</span>
      <strong>{order.grandTotalAmount} {order.currencyCode}</strong>
      <a data-testid={`order-history-details-${order.orderNumber}`} href={`/order/order-history/${order.orderNumber}`}>{t('order.history.details')}</a>
    </article>
  );
}
