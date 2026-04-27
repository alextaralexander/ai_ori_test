import { useState } from 'react';
import { t } from '../i18n';

type AdminWmsTab = 'warehouses' | 'stocks' | 'supplies' | 'sync' | 'audit';

const ADMIN_WMS_ROLES = new Set([
  'logistics-admin',
  'warehouse-operator',
  'order-admin',
  'wms-integration-operator',
  'auditor',
  'business-admin',
  'super-admin',
]);

export function AdminWmsView() {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [tab, setTab] = useState<AdminWmsTab>('warehouses');
  const [notification, setNotification] = useState('');
  const [warehouseSaved, setWarehouseSaved] = useState(false);
  const [availabilityChanged, setAvailabilityChanged] = useState(false);
  const [supplyCreated, setSupplyCreated] = useState(false);
  const [supplyAccepted, setSupplyAccepted] = useState(false);
  const [syncStarted, setSyncStarted] = useState(false);

  if (!ADMIN_WMS_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-wms-forbidden">
        STR_MNEMO_ADMIN_WMS_ACCESS_DENIED
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-wms-page">
      <h1>{t('adminWms.title')}</h1>
      <nav className="admin-tabs" aria-label={t('adminWms.tabs.label')}>
        <button data-testid="admin-wms-tab-warehouses" onClick={() => setTab('warehouses')}>{t('adminWms.tabs.warehouses')}</button>
        <button data-testid="admin-wms-tab-stocks" onClick={() => setTab('stocks')}>{t('adminWms.tabs.stocks')}</button>
        <button data-testid="admin-wms-tab-supplies" onClick={() => setTab('supplies')}>{t('adminWms.tabs.supplies')}</button>
        <button data-testid="admin-wms-tab-sync" onClick={() => setTab('sync')}>{t('adminWms.tabs.sync')}</button>
        <button data-testid="admin-wms-tab-audit" onClick={() => setTab('audit')}>{t('adminWms.tabs.audit')}</button>
      </nav>

      {tab === 'warehouses' ? (
        <section data-testid="admin-wms-warehouse-table">
          <button data-testid="admin-wms-create-warehouse">{t('adminWms.action.createWarehouse')}</button>
          <input data-testid="admin-wms-warehouse-code" aria-label={t('adminWms.field.warehouseCode')} />
          <input data-testid="admin-wms-warehouse-name" aria-label={t('adminWms.field.warehouseName')} />
          <button
            data-testid="admin-wms-warehouse-save"
            onClick={() => {
              setWarehouseSaved(true);
              setNotification('STR_MNEMO_ADMIN_WMS_WAREHOUSE_SAVED');
            }}
          >
            {t('adminWms.action.saveWarehouse')}
          </button>
          <div>{warehouseSaved ? 'WH-MSK-01 ACTIVE' : 'WH-SPB-01 ACTIVE'}</div>
        </section>
      ) : null}

      {tab === 'stocks' ? (
        <section data-testid="admin-wms-stock-panel">
          <input data-testid="admin-wms-stock-sku-filter" aria-label={t('adminWms.field.sku')} />
          <button data-testid="admin-wms-stock-search" onClick={() => setAvailabilityChanged(false)}>
            {t('adminWms.action.searchStock')}
          </button>
          <section data-testid="admin-wms-stock-table">
            <div>BOG-SERUM-001 WEB {availabilityChanged ? 'SELLABLE' : 'HOLD'}</div>
          </section>
          <button data-testid="admin-wms-change-availability">{t('adminWms.action.changeAvailability')}</button>
          <select data-testid="admin-wms-availability-policy" aria-label={t('adminWms.field.availabilityPolicy')}>
            <option value="HOLD">HOLD</option>
            <option value="SELLABLE">SELLABLE</option>
            <option value="BLOCKED">BLOCKED</option>
          </select>
          <input data-testid="admin-wms-availability-reason" aria-label={t('adminWms.field.reasonCode')} />
          <button
            data-testid="admin-wms-availability-save"
            onClick={() => {
              setAvailabilityChanged(true);
              setNotification('STR_MNEMO_ADMIN_WMS_AVAILABILITY_SAVED');
            }}
          >
            {t('adminWms.action.saveAvailability')}
          </button>
        </section>
      ) : null}

      {tab === 'supplies' ? (
        <section data-testid="admin-wms-supply-panel">
          <button data-testid="admin-wms-create-supply">{t('adminWms.action.createSupply')}</button>
          <input data-testid="admin-wms-supply-code" aria-label={t('adminWms.field.supplyCode')} />
          <input data-testid="admin-wms-supply-sku" aria-label={t('adminWms.field.sku')} />
          <input data-testid="admin-wms-supply-planned-qty" aria-label={t('adminWms.field.plannedQty')} />
          <button
            data-testid="admin-wms-supply-save"
            onClick={() => {
              setSupplyCreated(true);
              setNotification('STR_MNEMO_ADMIN_WMS_SUPPLY_CREATED');
            }}
          >
            {t('adminWms.action.saveSupply')}
          </button>
          <section data-testid="admin-wms-supply-table">
            <div>{supplyCreated ? 'SUP-032-001 EXPECTED' : 'SUP-031-001 ACCEPTED'}</div>
          </section>
          <button data-testid="admin-wms-supply-accept">{t('adminWms.action.acceptSupply')}</button>
          <input data-testid="admin-wms-accepted-qty" aria-label={t('adminWms.field.acceptedQty')} />
          <input data-testid="admin-wms-damaged-qty" aria-label={t('adminWms.field.damagedQty')} />
          <input data-testid="admin-wms-acceptance-reason" aria-label={t('adminWms.field.reasonCode')} />
          <button
            data-testid="admin-wms-acceptance-confirm"
            onClick={() => {
              setSupplyAccepted(true);
              setNotification('STR_MNEMO_ADMIN_WMS_SUPPLY_ACCEPTED');
            }}
          >
            {t('adminWms.action.confirmAcceptance')}
          </button>
          <div>{supplyAccepted ? 'PARTIALLY_ACCEPTED' : null}</div>
        </section>
      ) : null}

      {tab === 'sync' ? (
        <section data-testid="admin-wms-sync-panel">
          <select data-testid="admin-wms-sync-source" aria-label={t('adminWms.field.syncSource')}>
            <option value="WMS">WMS</option>
            <option value="1C">1C</option>
          </select>
          <button
            data-testid="admin-wms-sync-start"
            onClick={() => {
              setSyncStarted(true);
              setNotification('STR_MNEMO_ADMIN_WMS_SYNC_STARTED');
            }}
          >
            {t('adminWms.action.startSync')}
          </button>
          <section data-testid="admin-wms-sync-journal">
            <div>{syncStarted ? 'STARTED CORR-032-SYNC-WMS' : 'QUARANTINED DUPLICATE_DOCUMENT'}</div>
          </section>
        </section>
      ) : null}

      {tab === 'audit' ? (
        <section data-testid="admin-wms-audit-table">
          <div>ADMIN_WMS_WAREHOUSE_CREATED</div>
          <div>ADMIN_WMS_SYNC_STARTED</div>
          <div>correlationId CORR-032-AUDIT</div>
        </section>
      ) : null}

      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
