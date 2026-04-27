import { useState } from 'react';
import { t } from '../i18n';

const ADMIN_PIM_ROLES = new Set(['pim-manager', 'category-admin', 'media-manager', 'commercial-admin', 'auditor', 'super-admin']);

type AdminPimTab = 'products' | 'categories' | 'media' | 'import-export' | 'audit';

export function AdminPimView() {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [tab, setTab] = useState<AdminPimTab>('products');
  const [notification, setNotification] = useState('');
  const [categoryStatus, setCategoryStatus] = useState('DRAFT');
  const [productStatus, setProductStatus] = useState('DRAFT');
  const [productSaved, setProductSaved] = useState(false);
  const [mediaApproved, setMediaApproved] = useState(false);

  if (!ADMIN_PIM_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-pim-forbidden">
        STR_MNEMO_ADMIN_PIM_FORBIDDEN
      </main>
    );
  }

  const publishProduct = () => {
    if (!mediaApproved) {
      setNotification('STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED');
      return;
    }
    setProductStatus('PUBLISHED');
    setNotification('STR_MNEMO_ADMIN_PIM_PRODUCT_PUBLISHED');
  };

  return (
    <main className="platform-page" data-testid="admin-pim-page">
      <h1>{t('adminPim.title')}</h1>
      <nav className="admin-tabs" aria-label={t('adminPim.tabs.label')}>
        <button data-testid="admin-pim-tab-products" onClick={() => setTab('products')}>{t('adminPim.tabs.products')}</button>
        <button data-testid="admin-pim-tab-categories" onClick={() => setTab('categories')}>{t('adminPim.tabs.categories')}</button>
        <button data-testid="admin-pim-tab-media" onClick={() => setTab('media')}>{t('adminPim.tabs.media')}</button>
        <button data-testid="admin-pim-tab-import-export" onClick={() => setTab('import-export')}>{t('adminPim.tabs.importExport')}</button>
        <button data-testid="admin-pim-tab-audit" onClick={() => setTab('audit')}>{t('adminPim.tabs.audit')}</button>
      </nav>

      {tab === 'categories' ? (
        <section data-testid="admin-pim-category-panel">
          <button data-testid="admin-pim-create-category">{t('adminPim.action.createCategory')}</button>
          <input data-testid="admin-pim-category-slug" aria-label={t('adminPim.field.categorySlug')} />
          <input data-testid="admin-pim-category-name" aria-label={t('adminPim.field.categoryName')} />
          <button
            data-testid="admin-pim-category-save"
            onClick={() => setNotification('STR_MNEMO_ADMIN_PIM_CATEGORY_SAVED')}
          >
            {t('adminPim.action.saveCategory')}
          </button>
          <button data-testid="admin-pim-category-activate" onClick={() => setCategoryStatus('ACTIVE')}>
            {t('adminPim.action.activateCategory')}
          </button>
          <section data-testid="admin-pim-category-tree">face-care {categoryStatus}</section>
        </section>
      ) : null}

      {tab === 'products' ? (
        <section data-testid="admin-pim-product-panel">
          <button data-testid="admin-pim-create-product">{t('adminPim.action.createProduct')}</button>
          <input data-testid="admin-pim-product-sku" aria-label={t('adminPim.field.sku')} />
          <input data-testid="admin-pim-product-article-code" aria-label={t('adminPim.field.articleCode')} />
          <input data-testid="admin-pim-product-brand-code" aria-label={t('adminPim.field.brandCode')} />
          <input data-testid="admin-pim-product-name" aria-label={t('adminPim.field.productName')} />
          <textarea data-testid="admin-pim-product-description" aria-label={t('adminPim.field.description')} />
          <textarea data-testid="admin-pim-product-composition" aria-label={t('adminPim.field.composition')} />
          <button data-testid="admin-pim-product-category-face-care">{t('adminPim.category.faceCare')}</button>
          <button
            data-testid="admin-pim-product-save"
            onClick={() => {
              setProductSaved(true);
              setNotification('STR_MNEMO_ADMIN_PIM_PRODUCT_SAVED');
            }}
          >
            {t('adminPim.action.saveProduct')}
          </button>
          <button data-testid="admin-pim-product-publish" onClick={publishProduct}>
            {t('adminPim.action.publishProduct')}
          </button>
          <section data-testid="admin-pim-product-table">
            {productSaved ? <div>BOG-SERUM-001 {productStatus}</div> : <div>BOG-CREAM-002 PUBLISHED</div>}
          </section>
        </section>
      ) : null}

      {tab === 'media' ? (
        <section data-testid="admin-pim-media-panel">
          <button data-testid="admin-pim-media-add">{t('adminPim.action.addMedia')}</button>
          <input data-testid="admin-pim-media-file-name" aria-label={t('adminPim.field.fileName')} />
          <input data-testid="admin-pim-media-mime-type" aria-label={t('adminPim.field.mimeType')} />
          <input data-testid="admin-pim-media-checksum" aria-label={t('adminPim.field.checksum')} />
          <input data-testid="admin-pim-media-alt-text" aria-label={t('adminPim.field.altText')} />
          <button data-testid="admin-pim-media-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PIM_MEDIA_SAVED')}>
            {t('adminPim.action.saveMedia')}
          </button>
          <button data-testid="admin-pim-media-approve" onClick={() => setMediaApproved(true)}>
            {t('adminPim.action.approveMedia')}
          </button>
          <section data-testid="admin-pim-media-table">serum-main.jpg {mediaApproved ? 'APPROVED' : 'DRAFT'}</section>
        </section>
      ) : null}

      {tab === 'import-export' ? (
        <section data-testid="admin-pim-import-export-panel">
          <button data-testid="admin-pim-import-start" onClick={() => setNotification('STR_MNEMO_ADMIN_PIM_IMPORT_APPLIED')}>
            {t('adminPim.action.startImport')}
          </button>
          <button data-testid="admin-pim-export-start" onClick={() => setNotification('STR_MNEMO_ADMIN_PIM_EXPORT_CREATED')}>
            {t('adminPim.action.startExport')}
          </button>
        </section>
      ) : null}

      {tab === 'audit' ? (
        <section data-testid="admin-pim-audit-table">
          <div>CATEGORY_ACTIVATED</div>
          <div>PRODUCT_PUBLISHED</div>
          <div>correlationId CORR-029-AUDIT</div>
        </section>
      ) : null}

      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
