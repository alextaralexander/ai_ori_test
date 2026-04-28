import { Alert, Button, Input, Select, Space, Steps, Table } from 'antd';
import type { ReactElement } from 'react';
import { useState } from 'react';
import { t } from '../i18n';

const pickupPoint = {
  code: 'PVZ-MSK-037',
  addressLine: 'Moscow, Delivery street, 37',
  storageLimitDays: 7,
  workSchedule: '10:00-22:00'
};

const trackingItems = [
  'ORDER_CONFIRMED',
  'SHIPPED',
  'IN_TRANSIT',
  'READY_FOR_PICKUP'
];

export function CheckoutDeliverySection(): ReactElement {
  const [method, setMethod] = useState('COURIER_DELIVERY');
  const [query, setQuery] = useState('');
  const [selectedPickup, setSelectedPickup] = useState(false);
  const [notification, setNotification] = useState<string | null>(null);

  return (
    <section className="delivery-panel" data-testid="checkout-delivery-section">
      <h2>{t('delivery.checkout.title')}</h2>
      <Space wrap>
        <Button data-testid="delivery-method-home" onClick={() => setMethod('HOME_DELIVERY')} type={method === 'HOME_DELIVERY' ? 'primary' : 'default'}>
          {t('delivery.method.home')}
        </Button>
        <Button data-testid="delivery-method-courier" onClick={() => setMethod('COURIER_DELIVERY')} type={method === 'COURIER_DELIVERY' ? 'primary' : 'default'}>
          {t('delivery.method.courier')}
        </Button>
        <Button data-testid="delivery-method-pickup-point" onClick={() => setMethod('PICKUP_POINT')} type={method === 'PICKUP_POINT' ? 'primary' : 'default'}>
          {t('delivery.method.pickupPoint')}
        </Button>
      </Space>
      {method === 'PICKUP_POINT' ? (
        <section className="delivery-pickup-search">
          <Space wrap>
            <Input data-testid="pickup-point-search-input" onChange={(event) => setQuery(event.target.value)} placeholder={t('delivery.pickup.search')} value={query} />
            <Button data-testid="pickup-point-search-submit" onClick={() => setSelectedPickup(true)} type="primary">
              {t('delivery.action.search')}
            </Button>
          </Space>
          <article className="delivery-pickup-card" data-testid="pickup-point-card">
            <strong>{pickupPoint.code}</strong>
            <span>{pickupPoint.addressLine}</span>
            <span>{t('delivery.pickup.schedule')}: {pickupPoint.workSchedule}</span>
            <code>storageLimitDays: {pickupPoint.storageLimitDays}</code>
            <Button data-testid="pickup-point-select" onClick={() => setSelectedPickup(true)} type={selectedPickup ? 'primary' : 'default'}>
              {t('delivery.action.selectPickupPoint')}
            </Button>
          </article>
        </section>
      ) : null}
      <Button data-testid="checkout-confirm-order" onClick={() => setNotification('STR_MNEMO_DELIVERY_SHIPMENT_CREATED')} type="primary">
        {t('delivery.action.confirmOrder')}
      </Button>
      <div data-testid="delivery-notification-root">
        {notification ? `${t(notification)} (${notification})` : null}
      </div>
    </section>
  );
}

export function DeliveryTrackingView({ forbidden = false }: { forbidden?: boolean }): ReactElement {
  if (forbidden) {
    return <Alert data-testid="delivery-forbidden" message={`${t('STR_MNEMO_DELIVERY_ACCESS_DENIED')} (STR_MNEMO_DELIVERY_ACCESS_DENIED)`} type="error" />;
  }
  return (
    <main className="delivery-page" data-testid="order-tracking-page">
      <h1>{t('delivery.tracking.title')}</h1>
      <Steps
        current={3}
        data-testid="order-tracking-timeline"
        direction="vertical"
        items={trackingItems.map((status) => ({ title: status, description: 'correlationId CORR-037' }))}
      />
      <div data-testid="platform-notification-root">{t('STR_MNEMO_DELIVERY_READY_FOR_PICKUP')} (STR_MNEMO_DELIVERY_READY_FOR_PICKUP)</div>
    </main>
  );
}

export function PickupOwnerCabinetView(): ReactElement {
  const [status, setStatus] = useState('ARRIVED_AT_PICKUP_POINT');
  const [verificationCode, setVerificationCode] = useState('');

  const rows = [{ key: 'SHIP-037', shipmentCode: 'SHIP-037', currentStatus: status, correlationId: 'CORR-037' }];
  return (
    <main className="delivery-page" data-testid="pickup-owner-cabinet">
      <h1>{t('delivery.pickupOwner.title')}</h1>
      <Table
        columns={[
          { title: t('delivery.field.shipment'), dataIndex: 'shipmentCode' },
          { title: t('delivery.field.status'), dataIndex: 'currentStatus' },
          { title: 'correlationId', dataIndex: 'correlationId' }
        ]}
        data-testid="pickup-owner-shipment-table"
        dataSource={rows}
        pagination={false}
      />
      <Space wrap>
        <Button data-testid="pickup-shipment-accept" onClick={() => setStatus('READY_FOR_PICKUP')} type="primary">
          {t('delivery.action.accept')}
        </Button>
        <Input data-testid="pickup-verification-code" onChange={(event) => setVerificationCode(event.target.value)} placeholder={t('delivery.field.verificationCode')} value={verificationCode} />
        <Button data-testid="pickup-shipment-deliver" onClick={() => setStatus('DELIVERED')}>
          {t('delivery.action.deliver')}
        </Button>
        <Button data-testid="pickup-shipment-partial" onClick={() => setStatus('PARTIALLY_DELIVERED')}>
          {t('delivery.action.partialDeliver')}
        </Button>
      </Space>
    </main>
  );
}

export function DeliveryOperatorJournalView(): ReactElement {
  const [domain, setDomain] = useState('DELIVERY');
  return (
    <main className="delivery-page" data-testid="delivery-operator-page">
      <h1>{t('delivery.operator.title')}</h1>
      <Space wrap>
        <Select data-testid="delivery-operator-domain" onChange={setDomain} options={[{ label: 'DELIVERY', value: 'DELIVERY' }, { label: 'PICKUP_POINT', value: 'PICKUP_POINT' }]} value={domain} />
      </Space>
      <section data-testid="delivery-operator-journal">
        <span>sourceSystem DELIVERY_PROVIDER</span>
        <span> reasonCode STR_MNEMO_DELIVERY_PROBLEM_RECORDED</span>
        <span> correlationId CORR-037</span>
      </section>
    </main>
  );
}
