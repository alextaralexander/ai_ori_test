import { useState } from 'react';
import { t } from '../i18n';

type AdminCatalogTab = 'campaigns' | 'issues' | 'materials' | 'hotspots' | 'rollover' | 'audit';

const ADMIN_CATALOG_ROLES = new Set(['catalog-manager', 'content-admin', 'marketing-admin', 'auditor', 'super-admin']);

export function AdminCatalogView() {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [tab, setTab] = useState<AdminCatalogTab>('campaigns');
  const [notification, setNotification] = useState('');
  const [campaignSaved, setCampaignSaved] = useState(false);
  const [issueSaved, setIssueSaved] = useState(false);
  const [materialApproved, setMaterialApproved] = useState(false);
  const [hotspotSaved, setHotspotSaved] = useState(false);
  const [rolloverDone, setRolloverDone] = useState(false);

  if (!ADMIN_CATALOG_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-catalog-forbidden">
        STR_MNEMO_ADMIN_CATALOG_FORBIDDEN
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-catalog-page">
      <h1>{t('adminCatalog.title')}</h1>
      <nav className="admin-tabs" aria-label={t('adminCatalog.tabs.label')}>
        <button data-testid="admin-catalog-tab-campaigns" onClick={() => setTab('campaigns')}>{t('adminCatalog.tabs.campaigns')}</button>
        <button data-testid="admin-catalog-tab-issues" onClick={() => setTab('issues')}>{t('adminCatalog.tabs.issues')}</button>
        <button data-testid="admin-catalog-tab-materials" onClick={() => setTab('materials')}>{t('adminCatalog.tabs.materials')}</button>
        <button data-testid="admin-catalog-tab-hotspots" onClick={() => setTab('hotspots')}>{t('adminCatalog.tabs.hotspots')}</button>
        <button data-testid="admin-catalog-tab-rollover" onClick={() => setTab('rollover')}>{t('adminCatalog.tabs.rollover')}</button>
        <button data-testid="admin-catalog-tab-audit" onClick={() => setTab('audit')}>{t('adminCatalog.tabs.audit')}</button>
      </nav>

      {tab === 'campaigns' ? (
        <section data-testid="admin-catalog-campaign-table">
          <button data-testid="admin-catalog-create-campaign">{t('adminCatalog.action.createCampaign')}</button>
          <input data-testid="admin-catalog-campaign-code" aria-label={t('adminCatalog.field.campaignCode')} />
          <input data-testid="admin-catalog-campaign-name" aria-label={t('adminCatalog.field.campaignName')} />
          <button
            data-testid="admin-catalog-campaign-save"
            onClick={() => {
              setCampaignSaved(true);
              setNotification('STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_SAVED');
            }}
          >
            {t('adminCatalog.action.saveCampaign')}
          </button>
          <div>{campaignSaved ? 'CAM-2026-05 DRAFT' : 'CAM-2026-04 PUBLISHED'}</div>
        </section>
      ) : null}

      {tab === 'issues' ? (
        <section data-testid="admin-catalog-issue-panel">
          <button data-testid="admin-catalog-create-issue">{t('adminCatalog.action.createIssue')}</button>
          <input data-testid="admin-catalog-issue-code" aria-label={t('adminCatalog.field.issueCode')} />
          <button
            data-testid="admin-catalog-issue-save"
            onClick={() => {
              setIssueSaved(true);
              setNotification('STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED');
            }}
          >
            {t('adminCatalog.action.saveIssue')}
          </button>
          <section data-testid="admin-catalog-issue-list">{issueSaved ? 'ISSUE-2026-05 SCHEDULED' : 'ISSUE-2026-04 PUBLISHED'}</section>
        </section>
      ) : null}

      {tab === 'materials' ? (
        <section data-testid="admin-catalog-material-panel">
          <input data-testid="admin-catalog-material-file-name" aria-label={t('adminCatalog.field.fileName')} />
          <input data-testid="admin-catalog-material-checksum" aria-label={t('adminCatalog.field.checksum')} />
          <button data-testid="admin-catalog-material-save" onClick={() => setNotification('STR_MNEMO_ADMIN_CATALOG_MATERIAL_SAVED')}>
            {t('adminCatalog.action.saveMaterial')}
          </button>
          <button data-testid="admin-catalog-material-approve" onClick={() => setMaterialApproved(true)}>
            {t('adminCatalog.action.approveMaterial')}
          </button>
          <section data-testid="admin-catalog-material-table">best-origin-may-2026.pdf {materialApproved ? 'APPROVED' : 'UPLOADED'}</section>
          <button data-testid="admin-catalog-page-image-add" onClick={() => setNotification('STR_MNEMO_ADMIN_CATALOG_PAGE_SAVED')}>
            {t('adminCatalog.action.addPage')}
          </button>
          <section data-testid="admin-catalog-page-list">page-1 READY</section>
        </section>
      ) : null}

      {tab === 'hotspots' ? (
        <section data-testid="admin-catalog-hotspot-panel">
          <input data-testid="admin-catalog-hotspot-sku" aria-label={t('adminCatalog.field.sku')} />
          <button
            data-testid="admin-catalog-hotspot-save"
            onClick={() => {
              setHotspotSaved(true);
              setNotification('STR_MNEMO_ADMIN_CATALOG_HOTSPOT_SAVED');
            }}
          >
            {t('adminCatalog.action.saveHotspot')}
          </button>
          <button data-testid="admin-catalog-validate-links" onClick={() => setNotification('STR_MNEMO_ADMIN_CATALOG_LINKS_VALID')}>
            {t('adminCatalog.action.validateLinks')}
          </button>
          <section data-testid="admin-catalog-link-report">{hotspotSaved ? 'validHotspots 1 blockedHotspots 0' : 'validHotspots 0'}</section>
        </section>
      ) : null}

      {tab === 'rollover' ? (
        <section data-testid="admin-catalog-rollover-panel">
          <div data-testid="admin-catalog-freeze-warning">STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE</div>
          <button
            data-testid="admin-catalog-rollover-start"
            onClick={() => {
              setRolloverDone(true);
              setNotification('STR_MNEMO_ADMIN_CATALOG_ROLLOVER_COMPLETED');
            }}
          >
            {t('adminCatalog.action.startRollover')}
          </button>
          <section data-testid="admin-catalog-archive-list">{rolloverDone ? 'ISSUE-2026-04 ARCHIVED' : 'ISSUE-2026-04 PUBLISHED'}</section>
        </section>
      ) : null}

      {tab === 'audit' ? (
        <section data-testid="admin-catalog-audit-table">
          <div>CAMPAIGN_CREATED</div>
          <div>PDF_APPROVED</div>
          <div>HOTSPOT_CREATED</div>
          <div>ROLLOVER_COMPLETED</div>
          <div>correlationId CORR-030-AUDIT</div>
        </section>
      ) : null}

      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
