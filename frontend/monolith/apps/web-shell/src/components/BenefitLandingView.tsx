import { Alert, Button, Card, Space, Tag } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import {
  loadBenefitLanding,
  registerBenefitLandingConversion,
  type BenefitCtaType,
  type BenefitLanding,
  type BenefitLandingCta,
  type BenefitLandingType
} from '../api/publicContent';
import { t } from '../i18n';

interface BenefitLandingViewProps {
  landingType: BenefitLandingType;
  code: string | null;
  campaignId: string | null;
  variant: string | null;
}

export function BenefitLandingView({ landingType, code, campaignId, variant }: BenefitLandingViewProps): ReactElement {
  const [landing, setLanding] = useState<BenefitLanding | null>(null);

  useEffect(() => {
    let active = true;
    loadBenefitLanding(landingType, code, campaignId, variant).then((result) => {
      if (!active) {
        return;
      }
      setLanding(result);
      void registerBenefitLandingConversion(result, 'VIEW', window.location.pathname).catch(() => undefined);
    });
    return () => {
      active = false;
    };
  }, [campaignId, code, landingType, variant]);

  if (!landing) {
    return <main className="benefit-landing-page" data-testid="benefit-landing-loading">{t('public.benefits.loading')}</main>;
  }

  const hero = landing.blocks.find((block) => block.blockType === 'HERO') ?? landing.blocks[0];
  const secondaryBlocks = landing.blocks.filter((block) => block.blockKey !== hero.blockKey);

  return (
    <main className="benefit-landing-page" data-testid="benefit-landing-page">
      <span className="sr-only" data-testid="benefit-landing-type">{landing.landingType}</span>
      <section className="benefit-hero">
        <div>
          <Tag>{t(`public.benefits.type.${landing.landingType}`)}</Tag>
          <h1>{t(hero.titleKey)}</h1>
          {hero.bodyKey ? <p>{t(hero.bodyKey)}</p> : null}
          <ReferralState landing={landing} />
          <Space wrap>
            {hero.ctas.map((cta) => (
              <BenefitCtaButton cta={cta} key={cta.ctaType} landing={landing} />
            ))}
          </Space>
        </div>
        <div className="benefit-hero-media" aria-hidden="true" />
      </section>
      <section className="benefit-blocks" data-testid="benefit-blocks">
        {secondaryBlocks.map((block) => (
          <Card className="benefit-block-card" key={block.blockKey}>
            <Tag>{t(`public.benefits.blockType.${block.blockType}`)}</Tag>
            <h2>{t(block.titleKey)}</h2>
            {block.bodyKey ? <p>{t(block.bodyKey)}</p> : null}
            {block.ctas.length > 0 ? (
              <Space wrap>
                {block.ctas.map((cta) => (
                  <BenefitCtaButton cta={cta} key={cta.ctaType} landing={landing} />
                ))}
              </Space>
            ) : null}
          </Card>
        ))}
      </section>
    </main>
  );
}

function ReferralState({ landing }: { landing: BenefitLanding }): ReactElement {
  const referral = landing.referral;
  return (
    <div className="benefit-referral">
      <span className="sr-only" data-testid="benefit-referral-status">{referral.status}</span>
      {referral.code ? <span className="sr-only" data-testid="benefit-referral-code">{referral.code}</span> : null}
      {referral.status === 'ACTIVE' && referral.sponsorPublicNameKey ? (
        <Alert
          message={t('public.benefits.referral.active')}
          description={t(referral.sponsorPublicNameKey)}
          type="success"
          showIcon
        />
      ) : null}
      {referral.messageCode ? (
        <Alert
          data-testid="benefit-referral-message"
          message={t(referral.messageCode)}
          type="warning"
          showIcon
        />
      ) : null}
    </div>
  );
}

function BenefitCtaButton({ cta, landing }: { cta: BenefitLandingCta; landing: BenefitLanding }): ReactElement {
  const target = buildTargetUrl(cta, landing);
  return (
    <Button
      data-testid={ctaTestId(cta.ctaType)}
      href={target}
      onClick={(event) => {
        event.preventDefault();
        void registerBenefitLandingConversion(landing, cta.ctaType, window.location.pathname)
          .catch(() => undefined)
          .finally(() => {
            window.location.href = target;
          });
      }}
      type={primaryCta(cta.ctaType) ? 'primary' : 'default'}
    >
      {t(cta.labelKey)}
    </Button>
  );
}

function buildTargetUrl(cta: BenefitLandingCta, landing: BenefitLanding): string {
  const target = new URL(cta.targetRoute, window.location.origin);
  if (cta.preserveReferralContext) {
    target.searchParams.set('landingType', landing.landingType);
    target.searchParams.set('variant', landing.variant);
    target.searchParams.set('campaignId', landing.campaignId);
    if (landing.referral.code) {
      target.searchParams.set('code', landing.referral.code);
    }
  }
  return `${target.pathname}${target.search}`;
}

function ctaTestId(ctaType: BenefitCtaType): string {
  return `benefit-cta-${ctaType.toLowerCase().replaceAll('_', '-')}`;
}

function primaryCta(ctaType: BenefitCtaType): boolean {
  return ctaType === 'REGISTER' || ctaType === 'REGISTER_PARTNER' || ctaType === 'INSTALL_APP';
}
