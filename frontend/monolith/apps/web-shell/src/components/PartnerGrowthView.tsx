import { Alert, Progress } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { getMlmCommunity, getMlmConversion, getMlmDashboard, getMlmPartnerCard, getMlmTeamActivity, getMlmUpgrade, MlmStructureApiError, type MlmCommunityResponse, type MlmConversionFunnelResponse, type MlmDashboardResponse, type MlmPartnerCardResponse, type MlmTeamActivityResponse, type MlmUpgradeResponse, type MlmVolume } from '../api/mlmStructure';
import { t } from '../i18n';

type PartnerGrowthMode = 'dashboard' | 'community' | 'conversion' | 'activity' | 'upgrade' | 'partner-card';

interface PartnerGrowthViewProps {
  mode: PartnerGrowthMode;
  params?: URLSearchParams;
  personNumber?: string;
}

export function PartnerGrowthView({ mode, params = new URLSearchParams(), personNumber }: PartnerGrowthViewProps): ReactElement {
  const [dashboard, setDashboard] = useState<MlmDashboardResponse | null>(null);
  const [community, setCommunity] = useState<MlmCommunityResponse | null>(null);
  const [conversion, setConversion] = useState<MlmConversionFunnelResponse | null>(null);
  const [activity, setActivity] = useState<MlmTeamActivityResponse | null>(null);
  const [upgrade, setUpgrade] = useState<MlmUpgradeResponse | null>(null);
  const [partnerCard, setPartnerCard] = useState<MlmPartnerCardResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    const nextParams = new URLSearchParams(params);
    if (!nextParams.has('campaignId')) {
      nextParams.set('campaignId', 'CAT-2026-05');
    }
    setMessageCode(null);
    setDashboard(null);
    setCommunity(null);
    setConversion(null);
    setActivity(null);
    setUpgrade(null);
    setPartnerCard(null);

    async function load(): Promise<void> {
      if (mode === 'dashboard') {
        const loaded = await getMlmDashboard(nextParams);
        if (active) setDashboard(loaded);
      } else if (mode === 'community') {
        const loaded = await getMlmCommunity(nextParams);
        if (active) setCommunity(loaded);
      } else if (mode === 'conversion') {
        const loaded = await getMlmConversion(nextParams);
        if (active) setConversion(loaded);
      } else if (mode === 'activity') {
        const loaded = await getMlmTeamActivity(nextParams);
        if (active) setActivity(loaded);
      } else if (mode === 'upgrade') {
        const loaded = await getMlmUpgrade(nextParams);
        if (active) setUpgrade(loaded);
      } else if (personNumber) {
        const loaded = await getMlmPartnerCard(personNumber, nextParams);
        if (active) setPartnerCard(loaded);
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof MlmStructureApiError ? error.code : 'STR_MNEMO_MLM_STRUCTURE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [mode, params, personNumber]);

  if (messageCode) {
    return <Alert data-testid="mlm-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (mode === 'community') {
    return (
      <main className="partner-growth-page" data-testid="mlm-community-page">
        <PageHeading titleKey="partnerGrowth.community.title" descriptionKey="partnerGrowth.description" />
        <section className="partner-growth-list">
          {community?.partners.map((partner) => (
            <article className="partner-growth-row" data-testid={`mlm-partner-node-${partner.personNumber}`} key={partner.personNumber}>
              <strong>{partner.personNumber}</strong>
              <span>{partner.displayName}</span>
              <span data-testid={`mlm-branch-${partner.branchId}`}>{partner.branchId}</span>
              <span>{partner.structureLevel}</span>
              <span>{partner.partnerStatus}</span>
              <span>{partner.groupVolume.amount} {partner.groupVolume.currencyCode}</span>
            </article>
          ))}
        </section>
      </main>
    );
  }

  if (mode === 'conversion') {
    return (
      <main className="partner-growth-page" data-testid="mlm-conversion-page">
        <PageHeading titleKey="partnerGrowth.conversion.title" descriptionKey="partnerGrowth.description" />
        <section className="partner-growth-metrics">
          <Metric testId="mlm-conversion-sent" titleKey="partnerGrowth.conversion.sent" value={`${conversion?.inviteSentCount ?? 0}`} />
          <Metric testId="mlm-conversion-activated" titleKey="partnerGrowth.conversion.activated" value={`${conversion?.activatedCount ?? 0}`} />
          <Metric testId="mlm-conversion-first-order" titleKey="partnerGrowth.conversion.firstOrder" value={`${conversion?.firstOrderCount ?? 0}`} />
          <Metric testId="mlm-conversion-rate" titleKey="partnerGrowth.conversion.rate" value={`${conversion?.conversionRatePercent ?? 0}%`} />
        </section>
      </main>
    );
  }

  if (mode === 'activity') {
    return (
      <main className="partner-growth-page" data-testid="mlm-team-activity-page">
        <PageHeading titleKey="partnerGrowth.activity.title" descriptionKey="partnerGrowth.description" />
        <section className="partner-growth-list">
          {activity?.items.map((item) => (
            <article className="partner-growth-row" data-testid={item.riskSignal ? `mlm-activity-risk-${item.personNumber}` : `mlm-activity-${item.personNumber}-${item.activityType}`} key={`${item.personNumber}-${item.activityType}`}>
              <strong>{item.personNumber}</strong>
              <span>{item.activityType}</span>
              <span>{item.activityStatus}</span>
              <span>{item.occurredAt}</span>
            </article>
          ))}
        </section>
      </main>
    );
  }

  if (mode === 'upgrade') {
    return <UpgradeView upgrade={upgrade} />;
  }

  if (mode === 'partner-card') {
    return (
      <main className="partner-growth-page" data-testid="mlm-partner-card-page">
        <PageHeading titleKey="partnerGrowth.partnerCard.title" descriptionKey="partnerGrowth.description" />
        {partnerCard ? (
          <section className="partner-growth-card" data-testid={`mlm-partner-card-${partnerCard.personNumber}`}>
            <h2>{partnerCard.personNumber}</h2>
            <p>{partnerCard.displayName}</p>
            <dl>
              <dt>{t('partnerGrowth.card.status')}</dt>
              <dd>{partnerCard.partnerStatus}</dd>
              <dt>{t('partnerGrowth.card.sponsor')}</dt>
              <dd>{partnerCard.sponsorPersonNumber ?? '-'}</dd>
              <dt>{t('partnerGrowth.card.groupVolume')}</dt>
              <dd>{formatVolume(partnerCard.groupVolume)}</dd>
            </dl>
            <Progress percent={Number(partnerCard.qualificationProgress.qualificationProgress)} />
            <div data-testid="mlm-partner-linked-actions">
              {Object.entries(partnerCard.linkedActions).map(([key, value]) => <span key={key}>{key}: {value}</span>)}
            </div>
          </section>
        ) : null}
      </main>
    );
  }

  return (
    <main className="partner-growth-page" data-testid="mlm-dashboard-page">
      <PageHeading titleKey="partnerGrowth.dashboard.title" descriptionKey="partnerGrowth.description" />
      <section className="partner-growth-metrics">
        <Metric testId="mlm-dashboard-personal-volume" titleKey="partnerGrowth.personalVolume" value={dashboard ? formatVolume(dashboard.personalVolume) : '-'} />
        <Metric testId="mlm-dashboard-group-volume" titleKey="partnerGrowth.groupVolume" value={dashboard ? formatVolume(dashboard.groupVolume) : '-'} />
        <Metric testId="mlm-dashboard-active-partners" titleKey="partnerGrowth.activePartners" value={`${dashboard?.activePartnerCount ?? 0}`} />
        <Metric testId="mlm-dashboard-rank" titleKey="partnerGrowth.rank" value={`${dashboard?.currentRank ?? '-'} -> ${dashboard?.nextRank ?? '-'}`} />
      </section>
      <section className="partner-growth-card">
        <h2>{t('partnerGrowth.upgrade.progress')}</h2>
        <Progress percent={Number(dashboard?.qualificationPercent ?? 0)} />
        <div>{dashboard?.nextActions.map((action) => <span key={action}>{action}</span>)}</div>
      </section>
    </main>
  );
}

function UpgradeView({ upgrade }: { upgrade: MlmUpgradeResponse | null }): ReactElement {
  return (
    <main className="partner-growth-page" data-testid="mlm-upgrade-page">
      <PageHeading titleKey="partnerGrowth.upgrade.title" descriptionKey="partnerGrowth.description" />
      <section className="partner-growth-card">
        <h2 data-testid="mlm-upgrade-next-rank">{upgrade?.currentRank ?? '-'} {'->'} {upgrade?.nextRank ?? '-'}</h2>
        <Progress percent={Number(upgrade?.qualificationProgress ?? 0)} />
        <div>{upgrade?.deadlineAt}</div>
      </section>
      <section className="partner-growth-list">
        {upgrade?.requirements.map((requirement) => (
          <article className="partner-growth-row" key={requirement.code}>
            <strong>{requirement.code}</strong>
            <span>{requirement.status}</span>
            <span>{requirement.currentValue} / {requirement.targetValue}</span>
          </article>
        ))}
      </section>
    </main>
  );
}

function PageHeading({ descriptionKey, titleKey }: { descriptionKey: string; titleKey: string }): ReactElement {
  return (
    <header className="partner-growth-heading">
      <h1>{t(titleKey)}</h1>
      <span>{t(descriptionKey)}</span>
    </header>
  );
}

function Metric({ testId, titleKey, value }: { testId: string; titleKey: string; value: string }): ReactElement {
  return (
    <article className="partner-growth-metric" data-testid={testId}>
      <span>{t(titleKey)}</span>
      <strong>{value}</strong>
    </article>
  );
}

function formatVolume(value: MlmVolume): string {
  return `${value.amount} ${value.currencyCode}`;
}
