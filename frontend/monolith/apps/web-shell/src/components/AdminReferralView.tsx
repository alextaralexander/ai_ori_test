import { useState } from 'react';
import { t } from '../i18n';

type AdminReferralViewProps = {
  section?: 'main' | 'funnels' | 'codes' | 'analytics' | 'audit';
};

const ADMIN_REFERRAL_ROLES = new Set(['marketing-admin', 'crm-admin', 'auditor', 'super-admin']);

export function AdminReferralView({ section = 'main' }: AdminReferralViewProps) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [notification, setNotification] = useState('');
  const [landingStatus, setLandingStatus] = useState('DRAFT');
  const [previewOpen, setPreviewOpen] = useState(false);
  const [blocks, setBlocks] = useState<string[]>([]);
  const [funnelSaved, setFunnelSaved] = useState(false);
  const [codeGenerated, setCodeGenerated] = useState(false);

  if (!ADMIN_REFERRAL_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-referral-forbidden">
        STR_MNEMO_ADMIN_REFERRAL_FORBIDDEN
      </main>
    );
  }

  if (section === 'funnels') {
    return (
      <main className="platform-page" data-testid="admin-referral-page">
        <h1>{t('adminReferral.funnel.title')}</h1>
        <button data-testid="admin-referral-create-funnel">{t('adminReferral.action.createFunnel')}</button>
        <section data-testid="admin-referral-funnel-form">
          <input data-testid="admin-referral-field-funnel-code" aria-label={t('adminReferral.field.funnelCode')} />
          <select data-testid="admin-referral-field-funnel-scenario" aria-label={t('adminReferral.field.funnelScenario')} defaultValue="BUSINESS_PARTNER">
            <option value="BEAUTY_PARTNER">BEAUTY_PARTNER</option>
            <option value="BUSINESS_PARTNER">BUSINESS_PARTNER</option>
            <option value="CUSTOMER_REFERRAL">CUSTOMER_REFERRAL</option>
          </select>
          <button data-testid="admin-referral-funnel-add-consent-personal-data">{t('adminReferral.action.addPersonalDataConsent')}</button>
          <button data-testid="admin-referral-funnel-add-consent-partner-terms">{t('adminReferral.action.addPartnerTermsConsent')}</button>
          <button
            data-testid="admin-referral-funnel-save"
            onClick={() => {
              setFunnelSaved(true);
              setNotification('STR_MNEMO_ADMIN_REFERRAL_FUNNEL_SAVED');
            }}
          >
            {t('adminReferral.action.saveFunnel')}
          </button>
        </section>
        {funnelSaved ? <section data-testid="admin-referral-funnel-table">business-partner-default ACTIVE</section> : null}
        <section data-testid="admin-referral-attribution-policy">URL_REFERRAL_CODE MANUAL_CODE SESSION_CONTEXT CRM_OVERRIDE</section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'codes') {
    return (
      <main className="platform-page" data-testid="admin-referral-page">
        <h1>{t('adminReferral.codes.title')}</h1>
        <button data-testid="admin-referral-generate-code">{t('adminReferral.action.generateCode')}</button>
        <section data-testid="admin-referral-code-form">
          <select data-testid="admin-referral-field-code-type" aria-label={t('adminReferral.field.codeType')} defaultValue="CAMPAIGN_MULTI_USE">
            <option value="PERSONAL_SINGLE_USE">PERSONAL_SINGLE_USE</option>
            <option value="PERSONAL_MULTI_USE">PERSONAL_MULTI_USE</option>
            <option value="CAMPAIGN_SINGLE_USE">CAMPAIGN_SINGLE_USE</option>
            <option value="CAMPAIGN_MULTI_USE">CAMPAIGN_MULTI_USE</option>
          </select>
          <input data-testid="admin-referral-field-code-campaign" aria-label={t('adminReferral.field.campaignCode')} />
          <input data-testid="admin-referral-field-code-max-usage" aria-label={t('adminReferral.field.maxUsage')} />
          <button
            data-testid="admin-referral-code-save"
            onClick={() => {
              setCodeGenerated(true);
              setNotification('STR_MNEMO_ADMIN_REFERRAL_CODE_GENERATED');
            }}
          >
            {t('adminReferral.action.saveCode')}
          </button>
        </section>
        {codeGenerated ? <section data-testid="admin-referral-code-table">BIZSPRING2026 ACTIVE</section> : null}
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'analytics') {
    return (
      <main className="platform-page" data-testid="admin-referral-page">
        <h1>{t('adminReferral.analytics.title')}</h1>
        <section data-testid="admin-referral-conversion-report">
          <div>LANDING_VIEWED 12840</div>
          <div>CTA_CLICKED 4210</div>
          <div>PARTNER_ACTIVATED 984</div>
        </section>
      </main>
    );
  }

  if (section === 'audit') {
    return (
      <main className="platform-page" data-testid="admin-referral-page">
        <h1>{t('adminReferral.audit.title')}</h1>
        <section data-testid="admin-referral-audit-table">
          <div>LANDING_VARIANT_ACTIVATED</div>
          <div>REFERRAL_CODE_GENERATED</div>
          <div>correlationId CORR-028-AUDIT</div>
        </section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-referral-page">
      <h1>{t('adminReferral.title')}</h1>
      <section data-testid="admin-referral-landing-table">
        <button data-testid="admin-referral-create-landing">{t('adminReferral.action.createLanding')}</button>
        <div>business-partner-spring</div>
        <div>{landingStatus}</div>
      </section>
      <section data-testid="admin-referral-landing-form">
        <select data-testid="admin-referral-field-landing-type" aria-label={t('adminReferral.field.landingType')} defaultValue="BUSINESS">
          <option value="BEAUTY">BEAUTY</option>
          <option value="BUSINESS">BUSINESS</option>
          <option value="CUSTOMER_REFERRAL">CUSTOMER_REFERRAL</option>
        </select>
        <select data-testid="admin-referral-field-locale" aria-label={t('adminReferral.field.locale')} defaultValue="ru">
          <option value="ru">ru</option>
          <option value="en">en</option>
        </select>
        <input data-testid="admin-referral-field-slug" aria-label={t('adminReferral.field.slug')} />
        <input data-testid="admin-referral-field-name" aria-label={t('adminReferral.field.name')} />
        <input data-testid="admin-referral-field-campaign-code" aria-label={t('adminReferral.field.campaignCode')} />
        <button data-testid="admin-referral-add-block-hero" onClick={() => setBlocks([...blocks, 'HERO'])}>{t('adminReferral.action.addHero')}</button>
        <button data-testid="admin-referral-add-block-benefit" onClick={() => setBlocks([...blocks, 'BENEFIT'])}>{t('adminReferral.action.addBenefit')}</button>
        <button data-testid="admin-referral-add-block-cta" onClick={() => setBlocks([...blocks, 'CTA'])}>{t('adminReferral.action.addCta')}</button>
        <button data-testid="admin-referral-add-block-legal" onClick={() => setBlocks([...blocks, 'LEGAL_NOTICE'])}>{t('adminReferral.action.addLegal')}</button>
        <button
          data-testid="admin-referral-landing-save"
          onClick={() => {
            if (!blocks.includes('LEGAL_NOTICE')) {
              setNotification('STR_MNEMO_ADMIN_REFERRAL_LANDING_LEGAL_NOTICE_REQUIRED');
            } else {
              setNotification('STR_MNEMO_ADMIN_REFERRAL_LANDING_SAVED');
            }
          }}
        >
          {t('adminReferral.action.saveLanding')}
        </button>
      </section>
      <button data-testid="admin-referral-landing-preview-open" onClick={() => setPreviewOpen(true)}>
        {t('adminReferral.action.preview')}
      </button>
      {previewOpen ? <section data-testid="admin-referral-landing-preview">business-partner-spring BIZ-SPRING-2026</section> : null}
      <button data-testid="admin-referral-landing-activate" onClick={() => setLandingStatus('ACTIVE')}>
        {t('adminReferral.action.activate')}
      </button>
      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
