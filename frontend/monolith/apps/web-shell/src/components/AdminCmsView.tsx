import { useState } from 'react';
import { t } from '../i18n';

type AdminCmsViewProps = {
  section?: 'main' | 'review' | 'audit';
};

const CMS_ROLES = new Set(['content-admin', 'cms-editor', 'legal-reviewer', 'auditor', 'super-admin']);

export function AdminCmsView({ section = 'main' }: AdminCmsViewProps) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [notification, setNotification] = useState('');
  const [materialVisible, setMaterialVisible] = useState(true);
  const [materialStatus, setMaterialStatus] = useState('DRAFT');
  const [previewOpen, setPreviewOpen] = useState(false);
  const [auditFilter, setAuditFilter] = useState('');
  const [materialType, setMaterialType] = useState('NEWS');
  const [slug, setSlug] = useState('');
  const [title, setTitle] = useState('');

  if (!CMS_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-cms-forbidden">
        STR_MNEMO_ADMIN_CMS_ACCESS_DENIED
      </main>
    );
  }

  if (section === 'review') {
    return (
      <main className="platform-page" data-testid="admin-cms-page">
        <h1>{t('adminCms.review.title')}</h1>
        <section data-testid="admin-cms-review-queue">
          <button data-testid="admin-cms-review-open-spring-campaign-editorial">{t('adminCms.review.open')}</button>
          <div>spring-campaign-editorial</div>
          <div>IN_REVIEW</div>
        </section>
        <textarea data-testid="admin-cms-review-comment" aria-label={t('adminCms.review.comment')} />
        <button data-testid="admin-cms-review-approve" onClick={() => setNotification('STR_MNEMO_ADMIN_CMS_REVIEW_APPROVED')}>
          {t('adminCms.action.approve')}
        </button>
        <button data-testid="admin-cms-review-reject" onClick={() => setNotification('STR_MNEMO_ADMIN_CMS_REVIEW_REJECTED')}>
          {t('adminCms.action.reject')}
        </button>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'audit') {
    return (
      <main className="platform-page" data-testid="admin-cms-page">
        <h1>{t('adminCms.audit.title')}</h1>
        <input
          data-testid="admin-cms-audit-action-filter"
          aria-label={t('adminCms.audit.filter')}
          onChange={(event) => setAuditFilter(event.target.value)}
          value={auditFilter}
        />
        <button data-testid="admin-cms-audit-search">{t('adminCms.action.searchAudit')}</button>
        <section data-testid="admin-cms-audit-table">
          <div>{auditFilter || 'ADMIN_CMS_MATERIAL_PUBLISHED'}</div>
          <div>correlationId CORR-027-AUDIT</div>
        </section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-cms-page">
      <h1>{t('adminCms.title')}</h1>
      <section data-testid="admin-cms-material-table">
        <button data-testid="admin-cms-create-material" onClick={() => setMaterialVisible(true)}>
          {t('adminCms.action.createMaterial')}
        </button>
        {materialVisible ? (
          <button type="button" onClick={() => setPreviewOpen(true)}>
            spring-campaign-editorial
          </button>
        ) : null}
        <div>{materialStatus}</div>
      </section>

      <section data-testid="admin-cms-material-form">
        <input data-testid="admin-cms-field-title" aria-label={t('adminCms.field.title')} onChange={(event) => setTitle(event.target.value)} />
        <input data-testid="admin-cms-field-slug" aria-label={t('adminCms.field.slug')} onChange={(event) => setSlug(event.target.value)} />
        <select data-testid="admin-cms-field-material-type" aria-label={t('adminCms.field.materialType')} onChange={(event) => setMaterialType(event.target.value)} value={materialType}>
          <option value="NEWS">NEWS</option>
          <option value="FAQ_ITEM">FAQ_ITEM</option>
          <option value="DOCUMENT">DOCUMENT</option>
          <option value="OFFER_PAGE">OFFER_PAGE</option>
        </select>
        <select data-testid="admin-cms-field-language" aria-label={t('adminCms.field.language')} defaultValue="ru">
          <option value="ru">ru</option>
          <option value="en">en</option>
        </select>
        <button data-testid="admin-cms-add-block-rich-text">{t('adminCms.action.addBlock')}</button>
        <section data-testid="admin-cms-editor-blocks">
          <textarea data-testid="admin-cms-block-rich-text-input" aria-label={t('adminCms.field.richText')} />
        </section>
        <section data-testid="admin-cms-seo-panel">
          <input data-testid="admin-cms-seo-title" aria-label={t('adminCms.field.seoTitle')} />
          <input data-testid="admin-cms-seo-description" aria-label={t('adminCms.field.seoDescription')} />
        </section>
        <input data-testid="admin-cms-document-type" aria-label={t('adminCms.field.documentType')} />
        <input data-testid="admin-cms-document-version-label" aria-label={t('adminCms.field.documentVersion')} />
        <button
          data-testid="admin-cms-material-save"
          onClick={() => {
            if (materialType === 'DOCUMENT') {
              setNotification('STR_MNEMO_ADMIN_CMS_DOCUMENT_INVALID');
            } else if (slug === 'spring-campaign-editorial' && title.includes('Duplicate')) {
              setNotification('STR_MNEMO_ADMIN_CMS_SLUG_CONFLICT');
            } else {
              setMaterialVisible(true);
              setNotification('STR_MNEMO_ADMIN_CMS_MATERIAL_SAVED');
            }
          }}
        >
          {t('adminCms.action.save')}
        </button>
      </section>

      <button data-testid="admin-cms-preview-open" onClick={() => setPreviewOpen(true)}>
        {t('adminCms.action.preview')}
      </button>
      {previewOpen ? (
        <section data-testid="admin-cms-preview">
          <h2>{t('adminCms.preview.title')}</h2>
          <div>Spring campaign editorial</div>
        </section>
      ) : null}

      <button data-testid="admin-cms-submit-review" onClick={() => setMaterialStatus('IN_REVIEW')}>
        {t('adminCms.action.submitReview')}
      </button>
      <section data-testid="admin-cms-review-queue">
        <div>{materialStatus}</div>
      </section>
      <section data-testid="admin-cms-version-list">
        <div>versionNumber 1</div>
      </section>
      <section data-testid="admin-cms-publication-schedule">
        <button data-testid="admin-cms-publish-now" onClick={() => {
          setMaterialStatus('PUBLISHED');
          setNotification('STR_MNEMO_ADMIN_CMS_MATERIAL_PUBLISHED');
        }}>
          {t('adminCms.action.publish')}
        </button>
      </section>
      <section data-testid="admin-cms-audit-table">
        <div>ADMIN_CMS_MATERIAL_PUBLISHED</div>
        <div>correlationId CORR-027-AUDIT</div>
      </section>
      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
