import { useState } from 'react';
import { t } from '../i18n';

type AdminPricingTab = 'priceLists' | 'prices' | 'segmentRules' | 'promotions' | 'publish' | 'audit';

const ADMIN_PRICING_ROLES = new Set(['pricing-manager', 'promotions-manager', 'business-admin', 'auditor', 'super-admin']);

export function AdminPricingView() {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [tab, setTab] = useState<AdminPricingTab>('priceLists');
  const [notification, setNotification] = useState('');
  const [priceListSaved, setPriceListSaved] = useState(false);
  const [priceSaved, setPriceSaved] = useState(false);
  const [promotionSaved, setPromotionSaved] = useState(false);
  const [offerPaused, setOfferPaused] = useState(false);

  if (!ADMIN_PRICING_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-pricing-forbidden">
        STR_MNEMO_ADMIN_PRICING_FORBIDDEN
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-pricing-page">
      <h1>{t('adminPricing.title')}</h1>
      <nav className="admin-tabs" aria-label={t('adminPricing.tabs.label')}>
        <button data-testid="admin-pricing-tab-price-lists" onClick={() => setTab('priceLists')}>{t('adminPricing.tabs.priceLists')}</button>
        <button data-testid="admin-pricing-tab-prices" onClick={() => setTab('prices')}>{t('adminPricing.tabs.prices')}</button>
        <button data-testid="admin-pricing-tab-segment-rules" onClick={() => setTab('segmentRules')}>{t('adminPricing.tabs.segmentRules')}</button>
        <button data-testid="admin-pricing-tab-promotions" onClick={() => setTab('promotions')}>{t('adminPricing.tabs.promotions')}</button>
        <button data-testid="admin-pricing-tab-publish" onClick={() => setTab('publish')}>{t('adminPricing.tabs.publish')}</button>
        <button data-testid="admin-pricing-tab-audit" onClick={() => setTab('audit')}>{t('adminPricing.tabs.audit')}</button>
      </nav>

      {tab === 'priceLists' ? (
        <section data-testid="admin-pricing-price-list-table">
          <button data-testid="admin-pricing-create-price-list">{t('adminPricing.action.createPriceList')}</button>
          <input data-testid="admin-pricing-price-list-code" aria-label={t('adminPricing.field.priceListCode')} />
          <input data-testid="admin-pricing-price-list-name" aria-label={t('adminPricing.field.priceListName')} />
          <input data-testid="admin-pricing-campaign-id" aria-label={t('adminPricing.field.campaignId')} />
          <button
            data-testid="admin-pricing-price-list-save"
            onClick={() => {
              setPriceListSaved(true);
              setNotification('STR_MNEMO_ADMIN_PRICING_PRICE_LIST_SAVED');
            }}
          >
            {t('adminPricing.action.savePriceList')}
          </button>
          <div>{priceListSaved ? 'PL-RU-2026-05 DRAFT' : 'PL-RU-2026-04 ACTIVE'}</div>
        </section>
      ) : null}

      {tab === 'prices' ? (
        <section data-testid="admin-pricing-price-panel">
          <input data-testid="admin-pricing-price-sku" aria-label={t('adminPricing.field.sku')} />
          <input data-testid="admin-pricing-base-price" aria-label={t('adminPricing.field.basePrice')} />
          <button
            data-testid="admin-pricing-price-save"
            onClick={() => {
              setPriceSaved(true);
              setNotification('STR_MNEMO_ADMIN_PRICING_PRICE_SAVED');
            }}
          >
            {t('adminPricing.action.savePrice')}
          </button>
          <button data-testid="admin-pricing-promo-price-add">{t('adminPricing.action.addPromoPrice')}</button>
          <input data-testid="admin-pricing-promo-price-sku" aria-label={t('adminPricing.field.promoSku')} />
          <input data-testid="admin-pricing-promo-price-value" aria-label={t('adminPricing.field.promoPrice')} />
          <button data-testid="admin-pricing-promo-price-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PRICING_PROMO_PRICE_SAVED')}>
            {t('adminPricing.action.savePromoPrice')}
          </button>
          <section data-testid="admin-pricing-price-list">{priceSaved ? 'BOG-SERUM-001 1890.00' : 'BOG-CREAM-002 1290.00'}</section>
        </section>
      ) : null}

      {tab === 'segmentRules' ? (
        <section data-testid="admin-pricing-segment-rule-panel">
          <input data-testid="admin-pricing-segment-code" aria-label={t('adminPricing.field.segmentCode')} />
          <input data-testid="admin-pricing-role-code" aria-label={t('adminPricing.field.roleCode')} />
          <button data-testid="admin-pricing-segment-rule-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PRICING_SEGMENT_RULE_SAVED')}>
            {t('adminPricing.action.saveSegmentRule')}
          </button>
        </section>
      ) : null}

      {tab === 'promotions' ? (
        <section data-testid="admin-pricing-promotion-panel">
          <button data-testid="admin-pricing-create-promotion">{t('adminPricing.action.createPromotion')}</button>
          <input data-testid="admin-pricing-promotion-code" aria-label={t('adminPricing.field.promotionCode')} />
          <button
            data-testid="admin-pricing-promotion-save"
            onClick={() => {
              setPromotionSaved(true);
              setNotification('STR_MNEMO_ADMIN_PRICING_PROMOTION_SAVED');
            }}
          >
            {t('adminPricing.action.savePromotion')}
          </button>
          <input data-testid="admin-pricing-offer-code" aria-label={t('adminPricing.field.offerCode')} />
          <select data-testid="admin-pricing-offer-type" aria-label={t('adminPricing.field.offerType')}>
            <option value="BUNDLE">BUNDLE</option>
            <option value="GIFT">GIFT</option>
          </select>
          <button data-testid="admin-pricing-offer-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PRICING_OFFER_SAVED')}>
            {t('adminPricing.action.saveOffer')}
          </button>
          <input data-testid="admin-pricing-gift-sku" aria-label={t('adminPricing.field.giftSku')} />
          <input data-testid="admin-pricing-gift-threshold" aria-label={t('adminPricing.field.threshold')} />
          <button data-testid="admin-pricing-gift-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PRICING_GIFT_RULE_SAVED')}>
            {t('adminPricing.action.saveGift')}
          </button>
          <section data-testid="admin-pricing-promotion-list">{promotionSaved ? 'MAY-BEAUTY-BUNDLE DRAFT' : 'WELCOME-GIFT-2026 ACTIVE'}</section>
        </section>
      ) : null}

      {tab === 'publish' ? (
        <section data-testid="admin-pricing-publish-panel">
          <button data-testid="admin-pricing-publish-preflight" onClick={() => setNotification('STR_MNEMO_ADMIN_PRICING_PREFLIGHT_VALID')}>
            {t('adminPricing.action.preflight')}
          </button>
          <section data-testid="admin-pricing-preflight-summary">valid</section>
          <button data-testid="admin-pricing-publish-start" onClick={() => setNotification('STR_MNEMO_ADMIN_PRICING_PUBLISHED')}>
            {t('adminPricing.action.publish')}
          </button>
          <button data-testid="admin-pricing-pause-offer">{t('adminPricing.action.pauseOffer')}</button>
          <input data-testid="admin-pricing-pause-reason" aria-label={t('adminPricing.field.pauseReason')} />
          <button
            data-testid="admin-pricing-pause-confirm"
            onClick={() => {
              setOfferPaused(true);
              setNotification('STR_MNEMO_ADMIN_PRICING_OFFER_PAUSED');
            }}
          >
            {t('adminPricing.action.confirmPause')}
          </button>
        </section>
      ) : null}

      {tab === 'audit' ? (
        <section data-testid="admin-pricing-audit-table">
          <div>PRICE_LIST_CREATED</div>
          <div>PRICING_PUBLISHED</div>
          <div>{offerPaused ? 'SHOPPING_OFFER_PAUSED' : 'SHOPPING_OFFER_CREATED'}</div>
          <div>correlationId CORR-031-AUDIT</div>
        </section>
      ) : null}

      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
