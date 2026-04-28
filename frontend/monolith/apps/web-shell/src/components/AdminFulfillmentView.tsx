import { useState } from 'react';
import { t } from '../i18n';

type AdminFulfillmentSection = 'dashboard' | 'conveyor' | 'delivery-services' | 'pickup-points' | 'pickup-owner';

const ADMIN_FULFILLMENT_ROLES = new Set(['fulfillment-admin', 'conveyor-operator', 'delivery-admin', 'pickup-network-admin', 'pickup-owner', 'support-operator', 'auditor', 'super-admin']);

export function AdminFulfillmentView({ section = 'dashboard' }: { section?: AdminFulfillmentSection }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [stage, setStage] = useState('PICK_PENDING');
  const [deliveryStatus, setDeliveryStatus] = useState('DRAFT');
  const [pickupPointStatus, setPickupPointStatus] = useState('DRAFT');
  const [pickupShipmentStatus, setPickupShipmentStatus] = useState('EXPECTED');

  if (!ADMIN_FULFILLMENT_ROLES.has(role)) {
    return <main className="platform-page" data-testid="admin-fulfillment-forbidden">STR_MNEMO_FULFILLMENT_ACCESS_DENIED</main>;
  }

  if (section === 'conveyor') {
    return (
      <main className="platform-page" data-testid="admin-fulfillment-page">
        <h1>{t('adminFulfillment.conveyor.title')}</h1>
        <label>{t('adminFulfillment.field.taskSearch')}<input data-testid="conveyor-task-search" /></label>
        <button data-testid="conveyor-task-open">{t('adminFulfillment.action.openTask')}</button>
        <label>{t('adminFulfillment.field.scanOrder')}<input data-testid="conveyor-scan-order" /></label>
        <button data-testid="conveyor-stage-pick" onClick={() => setStage('PICK_IN_PROGRESS')}>{t('adminFulfillment.action.pick')}</button>
        <button data-testid="conveyor-stage-pack" onClick={() => setStage('PACK_IN_PROGRESS')}>{t('adminFulfillment.action.pack')}</button>
        <button data-testid="conveyor-stage-sort" onClick={() => setStage('SORT_PENDING')}>{t('adminFulfillment.action.sort')}</button>
        <section data-testid="conveyor-current-stage">{stage}</section>
      </main>
    );
  }

  if (section === 'delivery-services') {
    return (
      <main className="platform-page" data-testid="admin-fulfillment-page">
        <h1>{t('adminFulfillment.delivery.title')}</h1>
        <button data-testid="delivery-service-create">{t('adminFulfillment.action.createDeliveryService')}</button>
        <label>{t('adminFulfillment.field.serviceCode')}<input data-testid="delivery-service-code" /></label>
        <label>{t('adminFulfillment.field.displayKey')}<input data-testid="delivery-service-display-key" /></label>
        <label>{t('adminFulfillment.field.endpointAlias')}<input data-testid="delivery-service-endpoint-alias" /></label>
        <button data-testid="delivery-service-save" onClick={() => setDeliveryStatus('DRAFT')}>{t('adminFulfillment.action.save')}</button>
        <button data-testid="delivery-service-activate" onClick={() => setDeliveryStatus('ACTIVE')}>{t('adminFulfillment.action.activate')}</button>
        <section data-testid="delivery-service-status">{deliveryStatus}</section>
      </main>
    );
  }

  if (section === 'pickup-points') {
    return (
      <main className="platform-page" data-testid="admin-fulfillment-page">
        <h1>{t('adminFulfillment.pickup.title')}</h1>
        <button data-testid="pickup-point-create">{t('adminFulfillment.action.createPickupPoint')}</button>
        <label>{t('adminFulfillment.field.pickupCode')}<input data-testid="pickup-point-code" /></label>
        <label>{t('adminFulfillment.field.owner')}<input data-testid="pickup-point-owner" /></label>
        <label>{t('adminFulfillment.field.address')}<input data-testid="pickup-point-address" /></label>
        <label>{t('adminFulfillment.field.storageLimit')}<input data-testid="pickup-point-storage-limit" /></label>
        <label>{t('adminFulfillment.field.shipmentLimit')}<input data-testid="pickup-point-shipment-limit" /></label>
        <button data-testid="pickup-point-save" onClick={() => setPickupPointStatus('DRAFT')}>{t('adminFulfillment.action.save')}</button>
        <button data-testid="pickup-point-activate" onClick={() => setPickupPointStatus('ACTIVE')}>{t('adminFulfillment.action.activate')}</button>
        <button data-testid="pickup-point-temporary-close">{t('adminFulfillment.action.temporaryClose')}</button>
        <label>{t('adminFulfillment.field.reasonCode')}<input data-testid="pickup-point-reason-code" /></label>
        <button data-testid="pickup-point-confirm-close" onClick={() => setPickupPointStatus('TEMPORARILY_CLOSED')}>{t('adminFulfillment.action.confirmClose')}</button>
        <section data-testid="pickup-point-status">{pickupPointStatus}</section>
      </main>
    );
  }

  if (section === 'pickup-owner') {
    return (
      <main className="platform-page" data-testid="admin-fulfillment-page">
        <h1>{t('adminFulfillment.pickupOwner.title')}</h1>
        <label>{t('adminFulfillment.field.shipmentSearch')}<input data-testid="pickup-shipment-search" /></label>
        <button data-testid="pickup-shipment-accept" onClick={() => setPickupShipmentStatus('ACCEPTED')}>{t('adminFulfillment.action.accept')}</button>
        <button data-testid="pickup-shipment-not-collected">{t('adminFulfillment.action.notCollected')}</button>
        <label>{t('adminFulfillment.field.reasonCode')}<input data-testid="pickup-shipment-reason-code" /></label>
        <button data-testid="pickup-shipment-confirm-not-collected" onClick={() => setPickupShipmentStatus('NOT_COLLECTED')}>{t('adminFulfillment.action.confirmNotCollected')}</button>
        <section data-testid="pickup-shipment-status">{pickupShipmentStatus}</section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-fulfillment-page">
      <h1>{t('adminFulfillment.title')}</h1>
      <label>{t('adminFulfillment.field.correlationId')}<input data-testid="fulfillment-filter-correlation-id" /></label>
      <button data-testid="fulfillment-filter-apply">{t('adminFulfillment.action.applyFilters')}</button>
      <section data-testid="fulfillment-dashboard-table">CORR-039-1 READY_TO_SHIP STR_MNEMO_FULFILLMENT_STAGE_READY_TO_SHIP</section>
    </main>
  );
}
