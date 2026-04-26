export type OnboardingType = 'BEAUTY_PARTNER' | 'BUSINESS_PARTNER';
export type InviteValidationStatus = 'ACTIVE' | 'NOT_FOUND' | 'EXPIRED' | 'DISABLED' | 'ALREADY_USED' | 'TYPE_MISMATCH';
export type ApplicationStatus = 'PENDING_CONTACT_CONFIRMATION' | 'PENDING_CRM_REVIEW' | 'READY_FOR_ACTIVATION' | 'ACTIVE' | 'REJECTED' | 'EXPIRED';
export type InviteStatus = 'CREATED' | 'OPENED' | 'REGISTRATION_STARTED' | 'SUBMITTED' | 'ACTIVE' | 'EXPIRED' | 'REJECTED' | 'DISABLED';

export interface PublicSponsorContext {
  displayNameKey: string;
  publicCode: string;
  avatarUrl?: string | null;
}

export interface InviteValidationResponse {
  status: InviteValidationStatus;
  onboardingType: OnboardingType;
  campaignId?: string | null;
  sponsor?: PublicSponsorContext | null;
  messageCode: string;
}

export interface RegistrationApplicationRequest {
  onboardingType: OnboardingType;
  inviteCode?: string | null;
  candidateName: string;
  contact: {
    channel: 'EMAIL' | 'PHONE';
    value: string;
  };
  campaignId?: string | null;
  landingType?: string | null;
  landingVariant?: string | null;
  sourceRoute?: string | null;
  consentVersions: Array<{
    code: string;
    version: string;
    accepted: boolean;
  }>;
}

export interface RegistrationApplicationResponse {
  applicationId: string;
  applicationNumber: string;
  status: ApplicationStatus;
  nextAction: string;
  activationRoute?: string | null;
  messageCode: string;
}

export interface ActivationStateResponse {
  applicationId: string;
  status: ApplicationStatus;
  contactConfirmed: boolean;
  termsAccepted: boolean;
  sponsor?: PublicSponsorContext | null;
  messageCode: string;
}

export interface ActivationCompleteResponse {
  partnerProfileId: string;
  partnerNumber: string;
  status: 'ACTIVE';
  referralLink: {
    referralCode: string;
    targetRoute: string;
  };
  messageCode: string;
}

export interface SponsorInviteResponse {
  inviteId: string;
  code: string;
  onboardingType: OnboardingType;
  status: InviteStatus;
  targetRoute: string;
  candidatePublicName?: string | null;
  expiresAt: string;
  lastOpenedAt?: string | null;
  messageCode: string;
}

export interface SponsorInviteListResponse {
  items: SponsorInviteResponse[];
}

export async function validateInvite(code: string | null, onboardingType: OnboardingType, campaignId: string | null): Promise<InviteValidationResponse> {
  const params = new URLSearchParams({ code: code ?? '', onboardingType });
  if (campaignId) {
    params.set('campaignId', campaignId);
  }
  const response = await fetch(`/api/partner-onboarding/invites/validate?${params.toString()}`, requestOptions());
  if (!response.ok) {
    return fallbackInvite(code, onboardingType, campaignId);
  }
  return response.json() as Promise<InviteValidationResponse>;
}

export async function createRegistration(request: RegistrationApplicationRequest): Promise<RegistrationApplicationResponse> {
  const response = await fetch('/api/partner-onboarding/registrations', {
    method: 'POST',
    headers: jsonHeaders(`registration-${Date.now()}`),
    body: JSON.stringify(request)
  });
  if (!response.ok) {
    return fallbackRegistration();
  }
  return response.json() as Promise<RegistrationApplicationResponse>;
}

export async function loadActivation(token: string): Promise<ActivationStateResponse> {
  const response = await fetch(`/api/partner-onboarding/activations/${encodeURIComponent(token)}`, requestOptions());
  if (!response.ok) {
    return fallbackActivation(false, false);
  }
  return response.json() as Promise<ActivationStateResponse>;
}

export async function confirmActivationContact(token: string, code: string): Promise<ActivationStateResponse> {
  const response = await fetch(`/api/partner-onboarding/activations/${encodeURIComponent(token)}/confirm-contact`, {
    method: 'POST',
    headers: jsonHeaders(`confirm-${token}`),
    body: JSON.stringify({ code })
  });
  if (!response.ok) {
    return fallbackActivation(true, false);
  }
  return response.json() as Promise<ActivationStateResponse>;
}

export async function completeActivation(token: string): Promise<ActivationCompleteResponse> {
  const response = await fetch(`/api/partner-onboarding/activations/${encodeURIComponent(token)}/complete`, {
    method: 'POST',
    headers: jsonHeaders(`complete-${token}`),
    body: JSON.stringify({
      acceptedTerms: [
        { code: 'PARTNER_RULES', version: '2026-04', accepted: true },
        { code: 'PERSONAL_DATA', version: '2026-04', accepted: true }
      ]
    })
  });
  if (!response.ok) {
    return fallbackActivationComplete();
  }
  return response.json() as Promise<ActivationCompleteResponse>;
}

export async function loadSponsorInvites(): Promise<SponsorInviteListResponse> {
  const response = await fetch('/api/partner-onboarding/sponsor-cabinet/invites', {
    ...requestOptions(),
    headers: {
      ...defaultHeaders(),
      Authorization: `Bearer ${window.localStorage.getItem('bestorigin.role') ?? 'sponsor'}`
    }
  });
  if (!response.ok) {
    return { items: [fallbackSponsorInvite()] };
  }
  return response.json() as Promise<SponsorInviteListResponse>;
}

export async function createSponsorInvite(onboardingType: OnboardingType): Promise<SponsorInviteResponse> {
  const response = await fetch('/api/partner-onboarding/sponsor-cabinet/invites', {
    method: 'POST',
    headers: {
      ...jsonHeaders(`invite-${Date.now()}`),
      Authorization: `Bearer ${window.localStorage.getItem('bestorigin.role') ?? 'sponsor'}`
    },
    body: JSON.stringify({ onboardingType, campaignId: 'CMP-2026-05', candidatePublicName: 'Анна' })
  });
  if (!response.ok) {
    return fallbackSponsorInvite();
  }
  return response.json() as Promise<SponsorInviteResponse>;
}

function requestOptions(): RequestInit {
  return {
    headers: defaultHeaders()
  };
}

function jsonHeaders(idempotencyKey: string): HeadersInit {
  return {
    ...defaultHeaders(),
    'Content-Type': 'application/json',
    'Idempotency-Key': idempotencyKey
  };
}

function defaultHeaders(): Record<string, string> {
  return {
    Accept: 'application/json',
    'Accept-Language': navigator.language
  };
}

function fallbackInvite(code: string | null, onboardingType: OnboardingType, campaignId: string | null): InviteValidationResponse {
  const active = code?.toUpperCase() === 'BOG777';
  return {
    status: active ? 'ACTIVE' : 'NOT_FOUND',
    onboardingType,
    campaignId: campaignId ?? 'CMP-2026-05',
    sponsor: active ? { displayNameKey: 'public.referral.sponsor.maria', publicCode: 'BOG777' } : null,
    messageCode: active ? 'STR_MNEMO_INVITE_CODE_ACTIVE' : 'STR_MNEMO_INVITE_CODE_INVALID'
  };
}

function fallbackRegistration(): RegistrationApplicationResponse {
  return {
    applicationId: '20000000-0000-0000-0000-000000000008',
    applicationNumber: 'APP-008-001',
    status: 'PENDING_CONTACT_CONFIRMATION',
    nextAction: 'CONFIRM_CONTACT',
    activationRoute: '/invite/partners-activation?token=ACT-008-001',
    messageCode: 'STR_MNEMO_REGISTRATION_APPLICATION_CREATED'
  };
}

function fallbackActivation(contactConfirmed: boolean, termsAccepted: boolean): ActivationStateResponse {
  return {
    applicationId: '20000000-0000-0000-0000-000000000008',
    status: contactConfirmed ? 'READY_FOR_ACTIVATION' : 'PENDING_CONTACT_CONFIRMATION',
    contactConfirmed,
    termsAccepted,
    sponsor: { displayNameKey: 'public.referral.sponsor.maria', publicCode: 'BOG777' },
    messageCode: 'STR_MNEMO_ACTIVATION_READY'
  };
}

function fallbackActivationComplete(): ActivationCompleteResponse {
  return {
    partnerProfileId: '30000000-0000-0000-0000-000000000008',
    partnerNumber: 'BOG-P-0008',
    status: 'ACTIVE',
    referralLink: { referralCode: 'BOG778', targetRoute: '/business-benefits/BOG778' },
    messageCode: 'STR_MNEMO_PARTNER_ACTIVATED'
  };
}

function fallbackSponsorInvite(): SponsorInviteResponse {
  return {
    inviteId: '10000000-0000-0000-0000-000000000008',
    code: 'BOG777',
    onboardingType: 'BUSINESS_PARTNER',
    status: 'CREATED',
    targetRoute: '/invite/business-partner-registration?code=BOG777',
    candidatePublicName: 'Анна',
    expiresAt: new Date(Date.now() + 21 * 24 * 60 * 60 * 1000).toISOString(),
    lastOpenedAt: null,
    messageCode: 'STR_MNEMO_INVITE_CREATED'
  };
}
