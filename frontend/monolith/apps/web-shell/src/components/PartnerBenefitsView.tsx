import { useState } from 'react';
import { t } from '../i18n';

type PartnerBenefitsMode = 'member' | 'support';

export function PartnerBenefitsView({ mode = 'member' }: { mode?: PartnerBenefitsMode }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [activeTab, setActiveTab] = useState<'benefits' | 'referral' | 'rewards'>('benefits');
  const [preview, setPreview] = useState('');
  const [redemption, setRedemption] = useState('');
  const [supportOpened, setSupportOpened] = useState(false);

  if (mode === 'support') {
    if (!['partner-support', 'employee-support', 'supervisor', 'admin'].includes(role)) {
      return <main className="platform-page" data-testid="partner-benefits-support-forbidden">{t('STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED')}</main>;
    }
    return (
      <main className="platform-page" data-testid="partner-benefits-support-page">
        <h1>{t('partnerBenefits.support.title')}</h1>
        <label>{t('partnerBenefits.support.search')}<input data-testid="partner-benefits-support-search" /></label>
        <button data-testid="partner-benefits-support-open" onClick={() => setSupportOpened(true)}>{t('partnerBenefits.action.open')}</button>
        <section data-testid="partner-benefits-support-timeline">
          {supportOpened ? 'CORR-040-WELCOME STR_MNEMO_PARTNER_BENEFITS_AVAILABLE CORR-040-REF STR_MNEMO_PARTNER_BENEFITS_REFERRAL_REWARDED' : t('partnerBenefits.support.empty')}
        </section>
      </main>
    );
  }

  if (!['partner', 'partner-leader', 'business-manager'].includes(role)) {
    return <main className="platform-page" data-testid="partner-benefits-forbidden">{t('STR_MNEMO_PARTNER_BENEFITS_ACCESS_DENIED')}</main>;
  }

  return (
    <main className="platform-page" data-testid="partner-benefits-page">
      <h1>{t('partnerBenefits.title')}</h1>
      <section data-testid="partner-benefits-catalog">{t('partnerBenefits.catalog')}: CAT-2026-08</section>
      <nav aria-label={t('partnerBenefits.tabs.label')}>
        <button data-testid="partner-benefits-tab-benefits" onClick={() => setActiveTab('benefits')}>{t('partnerBenefits.tabs.benefits')}</button>
        <button data-testid="partner-benefits-tab-referral" onClick={() => setActiveTab('referral')}>{t('partnerBenefits.tabs.referral')}</button>
        <button data-testid="partner-benefits-tab-rewards" onClick={() => setActiveTab('rewards')}>{t('partnerBenefits.tabs.rewards')}</button>
      </nav>

      {activeTab === 'benefits' ? (
        <section>
          <article data-testid="partner-benefit-welcome">
            <h2>{t('partnerBenefits.welcome.title')}</h2>
            <p>WELCOME AVAILABLE STR_MNEMO_PARTNER_BENEFITS_AVAILABLE</p>
          </article>
          <article data-testid="partner-benefit-free-delivery">
            <h2>{t('partnerBenefits.freeDelivery.title')}</h2>
            <p>FREE_DELIVERY AVAILABLE CHECKOUT</p>
          </article>
          <aside data-testid="partner-benefit-expiry-warning">STR_MNEMO_PARTNER_BENEFITS_EXPIRES_SOON</aside>
          <button data-testid="partner-benefit-apply-checkout" onClick={() => setPreview('applicable')}>{t('partnerBenefits.action.applyCheckout')}</button>
          <section data-testid="partner-benefit-apply-preview">{preview}</section>
        </section>
      ) : null}

      {activeTab === 'referral' ? (
        <section>
          <h2>{t('partnerBenefits.referral.title')}</h2>
          <section data-testid="partner-referral-link">https://bestorigin.test/r/REF-CAT-2026-08</section>
          <section data-testid="partner-referral-events">QUALIFIED CORR-040-REF</section>
        </section>
      ) : null}

      {activeTab === 'rewards' ? (
        <section data-testid="partner-reward-shop">
          <h2>{t('partnerBenefits.rewards.title')}</h2>
          <button data-testid="partner-reward-card-REWARD-SKINCARE-BOX">REWARD-SKINCARE-BOX</button>
          <button data-testid="partner-reward-redeem" onClick={() => setRedemption('RESERVED')}>{t('partnerBenefits.action.redeem')}</button>
          <section data-testid="partner-reward-redemption-status">{redemption}</section>
        </section>
      ) : null}
    </main>
  );
}
