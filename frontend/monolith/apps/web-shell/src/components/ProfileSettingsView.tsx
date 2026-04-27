import { Alert, Button, Input } from 'antd';
import type { ReactElement } from 'react';
import { useState } from 'react';
import { t } from '../i18n';

type ProfileSettingsMode = 'overview' | 'general' | 'contacts' | 'addresses' | 'documents' | 'security' | 'forbidden';

interface ProfileSettingsViewProps {
  mode: ProfileSettingsMode;
}

export function ProfileSettingsView({ mode }: ProfileSettingsViewProps): ReactElement {
  const [generalResult, setGeneralResult] = useState('');
  const [contactSaved, setContactSaved] = useState(false);
  const [contactVerification, setContactVerification] = useState('');
  const [addressSaved, setAddressSaved] = useState(false);
  const [documentSaved, setDocumentSaved] = useState(false);
  const [securityError, setSecurityError] = useState('');
  const [lastName, setLastName] = useState('');
  const [documentType, setDocumentType] = useState('PASSPORT');

  if (mode === 'forbidden') {
    return <Alert data-testid="profile-access-denied" message={`${t('STR_MNEMO_PROFILE_ACCESS_DENIED')} (STR_MNEMO_PROFILE_ACCESS_DENIED)`} type="error" />;
  }

  if (mode === 'general') {
    return (
      <main className="profile-page" data-testid="profile-general-page">
        <h1>{t('profile.general.title')}</h1>
        <label>
          {t('profile.general.firstName')}
          <Input data-testid="profile-first-name" />
        </label>
        <label>
          {t('profile.general.lastName')}
          <Input data-testid="profile-last-name" onChange={(event) => setLastName(event.target.value)} value={lastName} />
        </label>
        <label>
          {t('profile.general.language')}
          <select data-testid="profile-preferred-language">
            <option value="ru">{t('profile.language.ru')}</option>
            <option value="en">{t('profile.language.en')}</option>
          </select>
        </label>
        <Button data-testid="profile-general-save" onClick={() => setGeneralResult(lastName || 'STR_MNEMO_PROFILE_GENERAL_UPDATED')} type="primary">
          {t('profile.save')}
        </Button>
        {generalResult ? <Alert data-testid="profile-general-result" message={`${t('STR_MNEMO_PROFILE_GENERAL_UPDATED')} ${generalResult}`} type="success" /> : null}
      </main>
    );
  }

  if (mode === 'contacts') {
    return (
      <main className="profile-page" data-testid="profile-contacts-page">
        <h1>{t('profile.contacts.title')}</h1>
        <select data-testid="profile-contact-type">
          <option value="EMAIL">EMAIL</option>
          <option value="PHONE">PHONE</option>
        </select>
        <Input data-testid="profile-contact-value" placeholder={t('profile.contacts.value')} />
        <label>
          <input data-testid="profile-contact-primary" type="checkbox" />
          {t('profile.contacts.primary')}
        </label>
        <Button data-testid="profile-contact-save" onClick={() => setContactSaved(true)} type="primary">{t('profile.save')}</Button>
        <div data-testid="profile-contact-list">{contactSaved ? `EMAIL customer013 masked` : t('profile.contacts.empty')}</div>
        <Button data-testid="profile-contact-verify" disabled={!contactSaved} onClick={() => setContactVerification('REQUIRES_VERIFICATION')}>
          {t('profile.contacts.verify')}
        </Button>
        {contactVerification ? <Alert data-testid="profile-contact-verification-status" message={`${contactVerification} STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION`} type="info" /> : null}
      </main>
    );
  }

  if (mode === 'addresses') {
    return (
      <main className="profile-page" data-testid="profile-addresses-page">
        <h1>{t('profile.addresses.title')}</h1>
        <Input data-testid="profile-address-city" placeholder={t('profile.addresses.city')} />
        <Input data-testid="profile-address-street" placeholder={t('profile.addresses.street')} />
        <Input data-testid="profile-address-house" placeholder={t('profile.addresses.house')} />
        <Input data-testid="profile-address-postal-code" placeholder={t('profile.addresses.postalCode')} />
        <label>
          <input data-testid="profile-address-default" type="checkbox" />
          {t('profile.addresses.default')}
        </label>
        <Button data-testid="profile-address-save" onClick={() => setAddressSaved(true)} type="primary">{t('profile.save')}</Button>
        <div data-testid="profile-address-list">{addressSaved ? 'Москва, Тверская, 101000' : t('profile.addresses.empty')}</div>
        <div data-testid="profile-readiness-checkout">{addressSaved ? `${t('profile.readiness.ready')} CHECKOUT` : `${t('profile.readiness.incomplete')} CHECKOUT`}</div>
      </main>
    );
  }

  if (mode === 'documents') {
    return (
      <main className="profile-page" data-testid="profile-documents-page">
        <h1>{t('profile.documents.title')}</h1>
        <select data-testid="profile-document-type" onChange={(event) => setDocumentType(event.target.value)} value={documentType}>
          <option value="PASSPORT">PASSPORT</option>
          <option value="TAX_ID">TAX_ID</option>
        </select>
        <Input data-testid="profile-document-number" placeholder={t('profile.documents.number')} />
        <Button data-testid="profile-document-save" onClick={() => setDocumentSaved(true)} type="primary">{t('profile.save')}</Button>
        <div data-testid="profile-document-list">{documentSaved ? `${documentType} 45********56` : t('profile.documents.empty')}</div>
      </main>
    );
  }

  if (mode === 'security') {
    return (
      <main className="profile-page" data-testid="profile-security-page">
        <h1>{t('profile.security.title')}</h1>
        <Input.Password data-testid="profile-current-password" placeholder={t('profile.security.currentPassword')} />
        <Input.Password data-testid="profile-new-password" placeholder={t('profile.security.newPassword')} />
        <Button data-testid="profile-password-save" onClick={() => setSecurityError('STR_MNEMO_PROFILE_PASSWORD_WEAK')} type="primary">{t('profile.security.changePassword')}</Button>
        {securityError ? <Alert data-testid="profile-security-error" message={`${t(securityError)} (${securityError})`} type="error" /> : null}
      </main>
    );
  }

  return (
    <main className="profile-page" data-testid="profile-settings-page">
      <h1>{t('profile.title')}</h1>
      <section className="profile-section-grid">
        <a data-testid="profile-section-general" href="/profile-settings/general">{t('profile.general.title')}</a>
        <a data-testid="profile-section-contacts" href="/profile-settings/contacts">{t('profile.contacts.title')}</a>
        <a data-testid="profile-section-addresses" href="/profile-settings/addresses">{t('profile.addresses.title')}</a>
        <a data-testid="profile-section-documents" href="/profile-settings/documents">{t('profile.documents.title')}</a>
        <a data-testid="profile-section-security" href="/profile-settings/security">{t('profile.security.title')}</a>
      </section>
      <section className="profile-readiness">
        <div data-testid="profile-readiness-checkout">{t('profile.readiness.ready')} CHECKOUT</div>
        <div data-testid="profile-readiness-delivery">{t('profile.readiness.ready')} DELIVERY</div>
        <div data-testid="profile-readiness-claim">{t('profile.readiness.ready')} CLAIM</div>
      </section>
    </main>
  );
}
