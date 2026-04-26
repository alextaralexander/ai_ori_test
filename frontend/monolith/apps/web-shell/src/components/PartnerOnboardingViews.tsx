import { Alert, Button, Card, Checkbox, Form, Input, List, Radio, Space, Steps, Tag } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import {
  completeActivation,
  confirmActivationContact,
  createRegistration,
  createSponsorInvite,
  loadActivation,
  loadSponsorInvites,
  validateInvite,
  type ActivationCompleteResponse,
  type ActivationStateResponse,
  type InviteValidationResponse,
  type OnboardingType,
  type RegistrationApplicationResponse,
  type SponsorInviteResponse
} from '../api/partnerOnboarding';
import { t } from '../i18n';

interface RegistrationViewProps {
  onboardingType: OnboardingType;
  code: string | null;
  campaignId: string | null;
  landingType: string | null;
  variant: string | null;
}

export function PartnerRegistrationView({ onboardingType, code, campaignId, landingType, variant }: RegistrationViewProps): ReactElement {
  const [invite, setInvite] = useState<InviteValidationResponse | null>(null);
  const [result, setResult] = useState<RegistrationApplicationResponse | null>(null);

  useEffect(() => {
    let active = true;
    validateInvite(code, onboardingType, campaignId).then((response) => {
      if (active) {
        setInvite(response);
      }
    });
    return () => {
      active = false;
    };
  }, [campaignId, code, onboardingType]);

  async function submit(values: { name?: string; contact?: string; partnerRules?: boolean; personalData?: boolean }) {
    const response = await createRegistration({
      onboardingType,
      inviteCode: code,
      candidateName: values.name ?? '',
      contact: { channel: 'EMAIL', value: values.contact ?? '' },
      campaignId,
      landingType,
      landingVariant: variant,
      sourceRoute: window.location.pathname,
      consentVersions: [
        { code: 'PARTNER_RULES', version: '2026-04', accepted: Boolean(values.partnerRules) },
        { code: 'PERSONAL_DATA', version: '2026-04', accepted: Boolean(values.personalData) }
      ]
    });
    setResult(response);
  }

  return (
    <main className="partner-onboarding-page" data-testid="partner-registration-page">
      <span className="sr-only" data-testid="partner-registration-onboarding-type">{onboardingType}</span>
      <section className="partner-onboarding-grid">
        <Card>
          <Tag>{t(`partner.onboarding.type.${onboardingType}`)}</Tag>
          <h1>{t('partner.registration.title')}</h1>
          <Steps
            current={result ? 3 : 0}
            items={[
              { title: t('partner.registration.step.context') },
              { title: t('partner.registration.step.contact') },
              { title: t('partner.registration.step.consent') },
              { title: t('partner.registration.step.result') }
            ]}
          />
          <InviteState invite={invite} />
        </Card>
        <Card>
          <Form layout="vertical" onFinish={submit}>
            <Form.Item label={t('partner.registration.name')} name="name" rules={[{ required: true }]}>
              <Input data-testid="partner-registration-name" />
            </Form.Item>
            <Form.Item label={t('partner.registration.contact')} name="contact" rules={[{ required: true }]}>
              <Input data-testid="partner-registration-contact" />
            </Form.Item>
            <Form.Item name="partnerRules" valuePropName="checked">
              <Checkbox data-testid="partner-registration-consent-PARTNER_RULES">{t('partner.registration.consent.partnerRules')}</Checkbox>
            </Form.Item>
            <Form.Item name="personalData" valuePropName="checked">
              <Checkbox data-testid="partner-registration-consent-PERSONAL_DATA">{t('partner.registration.consent.personalData')}</Checkbox>
            </Form.Item>
            <Button data-testid="partner-registration-submit" htmlType="submit" type="primary">{t('partner.registration.submit')}</Button>
          </Form>
          {result ? (
            <Alert
              className="partner-onboarding-result"
              data-testid="partner-registration-next-action"
              message={t(result.messageCode)}
              description={<span data-testid="partner-registration-status">{result.status}</span>}
              type="success"
              showIcon
            />
          ) : null}
        </Card>
      </section>
    </main>
  );
}

function InviteState({ invite }: { invite: InviteValidationResponse | null }): ReactElement {
  if (!invite) {
    return <Alert message={t('partner.registration.invite.loading')} type="info" showIcon />;
  }
  return (
    <div className="partner-onboarding-invite">
      <span className="sr-only" data-testid="partner-registration-invite-status">{invite.status}</span>
      {invite.sponsor ? <span className="sr-only" data-testid="partner-registration-invite-code">{invite.sponsor.publicCode}</span> : null}
      {invite.status === 'ACTIVE' && invite.sponsor ? (
        <Alert data-testid="partner-registration-sponsor-context" message={t('partner.registration.invite.active')} description={t(invite.sponsor.displayNameKey)} type="success" showIcon />
      ) : (
        <Alert data-testid="partner-registration-message" message={t(invite.messageCode)} type="warning" showIcon />
      )}
    </div>
  );
}

export function PartnerActivationView({ token }: { token: string }): ReactElement {
  const [state, setState] = useState<ActivationStateResponse | null>(null);
  const [complete, setComplete] = useState<ActivationCompleteResponse | null>(null);

  useEffect(() => {
    let active = true;
    loadActivation(token).then((response) => {
      if (active) {
        setState(response);
      }
    });
    return () => {
      active = false;
    };
  }, [token]);

  async function confirmContact(values: { code?: string }) {
    setState(await confirmActivationContact(token, values.code ?? ''));
  }

  async function completeFlow() {
    const response = await completeActivation(token);
    setComplete(response);
    setState((current) => current ? { ...current, status: 'ACTIVE', contactConfirmed: true, termsAccepted: true } : current);
  }

  return (
    <main className="partner-onboarding-page" data-testid="partner-activation-page">
      <Card>
        <h1>{t('partner.activation.title')}</h1>
        <span className="sr-only" data-testid="partner-activation-status">{complete?.status ?? state?.status}</span>
        <span className="sr-only" data-testid="partner-activation-contact-confirmed">{String(state?.contactConfirmed ?? false)}</span>
        <Form layout="vertical" onFinish={confirmContact}>
          <Form.Item label={t('partner.activation.code')} name="code" rules={[{ required: true }]}>
            <Input data-testid="partner-activation-code" />
          </Form.Item>
          <Button data-testid="partner-activation-confirm-contact" htmlType="submit">{t('partner.activation.confirmContact')}</Button>
        </Form>
        <Checkbox className="partner-onboarding-consent" data-testid="partner-activation-consent-PARTNER_RULES">{t('partner.registration.consent.partnerRules')}</Checkbox>
        <Button data-testid="partner-activation-complete" onClick={completeFlow} type="primary">{t('partner.activation.complete')}</Button>
        {complete ? <Alert data-testid="partner-referral-link" message={t(complete.messageCode)} description={complete.referralLink.targetRoute} type="success" showIcon /> : null}
      </Card>
    </main>
  );
}

export function SponsorCabinetView(): ReactElement {
  const [items, setItems] = useState<SponsorInviteResponse[]>([]);
  const [selectedType, setSelectedType] = useState<OnboardingType>('BUSINESS_PARTNER');
  const [created, setCreated] = useState<SponsorInviteResponse | null>(null);

  useEffect(() => {
    let active = true;
    loadSponsorInvites().then((response) => {
      if (active) {
        setItems(response.items);
      }
    });
    return () => {
      active = false;
    };
  }, []);

  async function createInvite() {
    const response = await createSponsorInvite(selectedType);
    setCreated(response);
    setItems((current) => [response, ...current]);
  }

  return (
    <main className="partner-onboarding-page" data-testid="sponsor-cabinet-page">
      <section className="partner-onboarding-grid">
        <Card>
          <h1>{t('partner.sponsorCabinet.title')}</h1>
          <Radio.Group onChange={(event) => setSelectedType(event.target.value)} value={selectedType}>
            <Radio.Button value="BUSINESS_PARTNER">
              <span data-testid="sponsor-invite-type-business">{t('partner.onboarding.type.BUSINESS_PARTNER')}</span>
            </Radio.Button>
            <Radio.Button value="BEAUTY_PARTNER">{t('partner.onboarding.type.BEAUTY_PARTNER')}</Radio.Button>
          </Radio.Group>
          <Space className="partner-onboarding-actions">
            <Button data-testid="sponsor-create-invite">{t('partner.sponsorCabinet.createInvite')}</Button>
            <Button data-testid="sponsor-invite-submit" onClick={createInvite} type="primary">{t('partner.sponsorCabinet.submitInvite')}</Button>
          </Space>
          {created ? (
            <Alert
              message={t(created.messageCode)}
              description={<span data-testid="sponsor-invite-target-route">{created.targetRoute}</span>}
              type="success"
              showIcon
            />
          ) : null}
          {created ? <span className="sr-only" data-testid="sponsor-invite-created-status">{created.status}</span> : null}
        </Card>
        <Card>
          <List
            data-testid="sponsor-invite-list"
            dataSource={items}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  title={`${item.code} · ${item.status}`}
                  description={`${t(`partner.onboarding.type.${item.onboardingType}`)} · ${item.targetRoute}`}
                />
              </List.Item>
            )}
          />
        </Card>
      </section>
    </main>
  );
}
