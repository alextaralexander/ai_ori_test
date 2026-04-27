import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { createOrderClaim, getOrderClaimDetails, getOrderDetails, OrderApiError, searchOrderClaims, type ClaimResolution, type OrderClaimDetailsResponse, type OrderClaimPageResponse, type OrderDetailsResponse } from '../api/order';
import { t } from '../i18n';

interface OrderClaimsViewProps {
  claimId?: string | null;
  mode?: 'create' | 'history' | 'details';
  params?: URLSearchParams;
}

export function OrderClaimsView({ claimId, mode = 'history', params = new URLSearchParams() }: OrderClaimsViewProps): ReactElement {
  const [order, setOrder] = useState<OrderDetailsResponse | null>(null);
  const [claims, setClaims] = useState<OrderClaimPageResponse | null>(null);
  const [details, setDetails] = useState<OrderClaimDetailsResponse | null>(null);
  const [query, setQuery] = useState(params.get('query') ?? '');
  const [selectedSku, setSelectedSku] = useState(params.get('sku') ?? '100-011');
  const [reasonCode, setReasonCode] = useState('DAMAGED_ITEM');
  const [resolution, setResolution] = useState<ClaimResolution>('REFUND');
  const [comment, setComment] = useState('');
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    async function load(): Promise<void> {
      setMessageCode(null);
      if (mode === 'create') {
        const loaded = await getOrderDetails(params.get('orderNumber') ?? 'ORD-011-MAIN');
        if (active) {
          setOrder(loaded);
          setSelectedSku(params.get('sku') ?? loaded.items[0]?.sku ?? '100-011');
        }
        return;
      }
      if (claimId) {
        const loaded = await getOrderClaimDetails(claimId);
        if (active) {
          setDetails(loaded);
        }
        return;
      }
      const loaded = await searchOrderClaims(params);
      if (active) {
        setClaims(loaded);
      }
    }
    void load().catch((error: unknown) => {
      setMessageCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_ORDER_CLAIMS_EMPTY');
    });
    return () => {
      active = false;
    };
  }, [claimId, mode, params]);

  async function submitClaim(): Promise<void> {
    try {
      const result = await createOrderClaim({
        orderNumber: order?.orderNumber ?? params.get('orderNumber') ?? 'ORD-011-MAIN',
        reasonCode,
        requestedResolution: resolution,
        comment,
        items: [{ sku: selectedSku, quantity: 1 }],
      });
      setDetails(result);
      setMessageCode(result.publicReasonMnemo ?? 'STR_MNEMO_ORDER_CLAIM_CREATED');
    } catch (error: unknown) {
      setMessageCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_ORDER_CLAIM_VALIDATION_FAILED');
    }
  }

  async function submitSearch(): Promise<void> {
    const next = new URLSearchParams(params);
    if (query) {
      next.set('query', query);
    } else {
      next.delete('query');
    }
    setClaims(await searchOrderClaims(next));
  }

  if (messageCode === 'STR_MNEMO_ORDER_CLAIM_ACCESS_DENIED') {
    return <Alert data-testid="claim-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (mode === 'create') {
    return (
      <main className="order-claims-page" data-testid="claim-create-page">
        <header className="order-history-heading">
          <h1>{t('order.claims.create.title')}</h1>
          <span>{order?.orderNumber ?? params.get('orderNumber') ?? 'ORD-011-MAIN'}</span>
        </header>
        {messageCode ? <Alert data-testid={messageCode === 'STR_MNEMO_ORDER_CLAIM_CREATED' ? 'claim-result' : 'claim-error'} message={`${t(messageCode)} (${messageCode}) ${details?.claimId ?? ''}`} type={messageCode === 'STR_MNEMO_ORDER_CLAIM_CREATED' ? 'success' : 'error'} /> : null}
        <section className="claim-create-grid">
          <article data-testid="claim-create-items">
            <h2>{t('order.claims.items')}</h2>
            {(order?.items ?? []).map((item) => (
              <label className="claim-item-row" key={item.sku}>
                <input checked={selectedSku === item.sku} data-testid={`claim-item-${item.sku}`} onChange={() => setSelectedSku(item.sku)} type="checkbox" />
                <span>{item.productName} x{item.quantity}</span>
              </label>
            ))}
          </article>
          <article>
            <h2>{t('order.claims.reasonAndResolution')}</h2>
            <select data-testid="claim-reason" onChange={(event) => setReasonCode(event.target.value)} value={reasonCode}>
              <option value="DAMAGED_ITEM">{t('order.claims.reason.DAMAGED_ITEM')}</option>
              <option value="MISSING_ITEM">{t('order.claims.reason.MISSING_ITEM')}</option>
            </select>
            <select data-testid="claim-resolution" onChange={(event) => setResolution(event.target.value as ClaimResolution)} value={resolution}>
              <option value="REFUND">{t('order.claims.resolution.REFUND')}</option>
              <option value="REPLACEMENT">{t('order.claims.resolution.REPLACEMENT')}</option>
              <option value="MISSING_ITEM">{t('order.claims.resolution.MISSING_ITEM')}</option>
            </select>
            <Input.TextArea data-testid="claim-comment" onChange={(event) => setComment(event.target.value)} placeholder={t('order.claims.comment')} value={comment} />
            <Button data-testid="claim-submit" onClick={() => void submitClaim()} type="primary">{t('order.claims.submit')}</Button>
          </article>
        </section>
      </main>
    );
  }

  if (claimId) {
    return (
      <main className="order-claims-page" data-testid="claim-details-page">
        <Button href="/order/claims/claims-history">{t('order.claims.back')}</Button>
        <header className="order-history-heading">
          <h1>{details?.claimId ?? claimId}</h1>
          <span>{details?.status}</span>
        </header>
        {details?.publicReasonMnemo ? <Alert data-testid="claim-details-status" message={`${t(details.publicReasonMnemo)} (${details.publicReasonMnemo})`} type="info" /> : null}
        <section className="order-details-grid">
          <article data-testid="claim-details-items">
            <h2>{t('order.claims.items')}</h2>
            {details?.items.map((item) => <div key={item.sku}>{item.productName} x{item.quantity}</div>)}
          </article>
          <article>
            <h2>{t('order.claims.compensation')}</h2>
            <strong>{details?.refundAmount} {details?.currencyCode}</strong>
            {details?.partnerImpact ? <div data-testid="claim-details-partner-impact">{t('order.claims.partnerImpact')}: {details.businessVolumeDelta}</div> : null}
          </article>
        </section>
        <section data-testid="claim-details-events">
          <h2>{t('order.claims.events')}</h2>
          {details?.events.map((event) => <div key={`${event.eventType}-${event.occurredAt}`}>{event.descriptionMnemo ? t(event.descriptionMnemo) : event.publicStatus}</div>)}
        </section>
      </main>
    );
  }

  return (
    <main className="order-claims-page" data-testid="claim-history-page">
      <header className="order-history-heading">
        <h1>{t('order.claims.history.title')}</h1>
        <span>{t('order.claims.history.description')}</span>
      </header>
      <section className="order-history-filters">
        <Input data-testid="claim-history-search" onChange={(event) => setQuery(event.target.value)} placeholder={t('order.claims.history.search')} value={query} />
        <Button data-testid="claim-history-submit" onClick={() => void submitSearch()} type="primary">{t('order.claims.history.submit')}</Button>
      </section>
      <section className="order-history-list">
        {claims?.items.map((claim) => (
          <article className="order-history-card" data-testid={`claim-history-card-${claim.claimId}`} key={claim.claimId}>
            <h2>{claim.claimId}</h2>
            <span>{claim.orderNumber}</span>
            <span>{claim.status} / {claim.requestedResolution}</span>
            <strong>{claim.refundAmount} {claim.currencyCode}</strong>
            <a data-testid={`claim-history-details-${claim.claimId}`} href={`/order/claims/claims-history/${claim.claimId}`}>{t('order.claims.details')}</a>
          </article>
        ))}
      </section>
    </main>
  );
}
