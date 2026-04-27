import { Alert, Button, Input, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import {
  EmployeeApiError,
  approveEmployeeElevatedRequest,
  createEmployeeElevatedRequest,
  getEmployeeAddresses,
  getEmployeeContacts,
  getEmployeeDocuments,
  getEmployeeProfileGeneral,
  getEmployeeProfileSettings,
  getEmployeeSecurity,
  getEmployeeSuperUser,
  updateEmployeeProfileGeneral,
  type EmployeeAddressResponse,
  type EmployeeContactResponse,
  type EmployeeDocumentResponse,
  type EmployeeElevatedRequestResponse,
  type EmployeeElevatedSessionResponse,
  type EmployeeProfileGeneralResponse,
  type EmployeeProfileSettingsSummaryResponse,
  type EmployeeSecuritySummaryResponse,
  type EmployeeSuperUserDashboardResponse,
} from '../api/employee';
import { t } from '../i18n';

interface EmployeeProfileSettingsViewProps {
  section: 'overview' | 'general' | 'contacts' | 'addresses' | 'documents' | 'security' | 'super-user';
}

export function EmployeeProfileSettingsView({ section }: EmployeeProfileSettingsViewProps): ReactElement {
  const [summary, setSummary] = useState<EmployeeProfileSettingsSummaryResponse | null>(null);
  const [general, setGeneral] = useState<EmployeeProfileGeneralResponse | null>(null);
  const [contacts, setContacts] = useState<EmployeeContactResponse[]>([]);
  const [addresses, setAddresses] = useState<EmployeeAddressResponse[]>([]);
  const [documents, setDocuments] = useState<EmployeeDocumentResponse[]>([]);
  const [security, setSecurity] = useState<EmployeeSecuritySummaryResponse | null>(null);
  const [superUser, setSuperUser] = useState<EmployeeSuperUserDashboardResponse | null>(null);
  const [request, setRequest] = useState<EmployeeElevatedRequestResponse | null>(null);
  const [session, setSession] = useState<EmployeeElevatedSessionResponse | null>(null);
  const [displayName, setDisplayName] = useState('');
  const [timezone, setTimezone] = useState('');
  const [reasonText, setReasonText] = useState('');
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setMessageCode(null);

    async function load(): Promise<void> {
      if (section === 'overview') {
        const loaded = await getEmployeeProfileSettings();
        if (active) {
          setSummary(loaded);
        }
      }
      if (section === 'general') {
        const loaded = await getEmployeeProfileGeneral();
        if (active) {
          setGeneral(loaded);
          setDisplayName(loaded.displayName);
          setTimezone(loaded.timezone);
        }
      }
      if (section === 'contacts') {
        const loaded = await getEmployeeContacts();
        if (active) {
          setContacts(loaded.items);
        }
      }
      if (section === 'addresses') {
        const loaded = await getEmployeeAddresses();
        if (active) {
          setAddresses(loaded.items);
        }
      }
      if (section === 'documents') {
        const loaded = await getEmployeeDocuments();
        if (active) {
          setDocuments(loaded.items);
        }
      }
      if (section === 'security') {
        const loaded = await getEmployeeSecurity();
        if (active) {
          setSecurity(loaded);
        }
      }
      if (section === 'super-user') {
        const loaded = await getEmployeeSuperUser();
        if (active) {
          setSuperUser(loaded);
          setSession(loaded.activeSession ?? null);
          setReasonText(t('employee.superUser.defaultReason'));
        }
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [section]);

  const contactColumns: ColumnsType<EmployeeContactResponse> = useMemo(() => [
    { dataIndex: 'contactType', title: t('employee.profile.contacts.type') },
    { dataIndex: 'maskedValue', title: t('employee.profile.contacts.value') },
    { dataIndex: 'verificationStatus', title: t('employee.profile.contacts.status') },
  ], []);
  const addressColumns: ColumnsType<EmployeeAddressResponse> = useMemo(() => [
    { dataIndex: 'addressType', title: t('employee.profile.addresses.type') },
    { dataIndex: 'regionCode', title: t('employee.profile.addresses.region') },
    { dataIndex: 'city', title: t('employee.profile.addresses.city') },
    { dataIndex: 'addressLine', title: t('employee.profile.addresses.line') },
  ], []);
  const documentColumns: ColumnsType<EmployeeDocumentResponse> = useMemo(() => [
    { dataIndex: 'documentType', title: t('employee.profile.documents.type') },
    { dataIndex: 'maskedNumber', title: t('employee.profile.documents.number') },
    { dataIndex: 'verificationStatus', title: t('employee.profile.documents.status') },
    { dataIndex: 'fileReferenceId', title: t('employee.profile.documents.file') },
  ], []);

  if (messageCode) {
    return <Alert data-testid="employee-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (section === 'overview') {
    return (
      <main className="employee-page" data-testid="employee-profile-settings-page">
        <PageHeader titleKey="employee.profile.title" subtitleKey="employee.profile.description" />
        <section className="employee-grid">
          {summary?.sections.map((item) => (
            <a className="employee-card" href={item.route} key={item.sectionCode}>
              <h2>{t(`employee.profile.section.${item.sectionCode}`)}</h2>
              <span>{item.readinessState}</span>
              <span>{item.warningCodes.join(' ')}</span>
            </a>
          ))}
        </section>
      </main>
    );
  }

  if (section === 'general') {
    async function save(): Promise<void> {
      const loaded = await updateEmployeeProfileGeneral(displayName, timezone, general?.version ?? 1);
      setGeneral(loaded);
      setDisplayName(loaded.displayName);
      setTimezone(loaded.timezone);
    }

    return (
      <main className="employee-page">
        <PageHeader titleKey="employee.profile.general.title" subtitleKey="employee.profile.general.description" />
        <section className="employee-card employee-form" data-testid="employee-profile-general-form">
          <Input data-testid="employee-profile-display-name" value={displayName} onChange={(event) => setDisplayName(event.target.value)} />
          <Input data-testid="employee-profile-timezone" value={timezone} onChange={(event) => setTimezone(event.target.value)} />
          <Button data-testid="employee-profile-general-save" onClick={() => void save()} type="primary">{t('employee.profile.save')}</Button>
          <span>{general?.displayName}</span>
          <span>{general?.timezone}</span>
        </section>
      </main>
    );
  }

  if (section === 'contacts') {
    return <SectionTable titleKey="employee.profile.contacts.title" testId="employee-profile-contacts-list" columns={contactColumns} dataSource={contacts} rowKey="contactId" />;
  }
  if (section === 'addresses') {
    return <SectionTable titleKey="employee.profile.addresses.title" testId="employee-profile-addresses-list" columns={addressColumns} dataSource={addresses} rowKey="addressId" />;
  }
  if (section === 'documents') {
    return <SectionTable titleKey="employee.profile.documents.title" testId="employee-profile-documents-list" columns={documentColumns} dataSource={documents} rowKey="documentId" />;
  }
  if (section === 'security') {
    return (
      <main className="employee-page">
        <PageHeader titleKey="employee.profile.security.title" subtitleKey="employee.profile.security.description" />
        <section className="employee-card" data-testid="employee-profile-security-panel">
          <strong>mfaEnabled: {String(security?.mfaEnabled)}</strong>
          <span>{t('employee.profile.security.sessions')}: {security?.activeSessionCount}</span>
          <span>{security?.recentEvents.map((item) => item.eventType).join(' ')}</span>
        </section>
      </main>
    );
  }

  async function requestElevated(): Promise<void> {
    const loaded = await createEmployeeElevatedRequest(reasonText);
    setRequest(loaded);
  }

  async function approveFirst(): Promise<void> {
    const requestId = request?.requestId ?? superUser?.pendingRequests[0]?.requestId;
    if (!requestId) {
      return;
    }
    const loaded = await approveEmployeeElevatedRequest(requestId);
    setSession(loaded);
  }

  return (
    <main className="employee-page" data-testid="employee-super-user-page">
      <PageHeader titleKey="employee.superUser.title" subtitleKey="employee.superUser.description" />
      {session ? <section className="employee-card" data-testid="employee-elevated-session-banner">{session.status} {session.elevatedSessionId} {session.remainingSeconds}</section> : null}
      <section className="employee-card" data-testid="employee-super-user-policy-list">
        {superUser?.policies.map((item) => <span key={item.policyCode}>{item.policyCode} {item.deniedCode}</span>)}
      </section>
      <section className="employee-card employee-form" data-testid="employee-super-user-request-form">
        <Input data-testid="employee-super-user-policy-code" defaultValue="EMPLOYEE_ELEVATED_SUPPORT_OPERATIONS" />
        <Input data-testid="employee-super-user-reason-code" defaultValue="SUPPORT_ESCALATION" />
        <Input data-testid="employee-super-user-reason-text" value={reasonText} onChange={(event) => setReasonText(event.target.value)} />
        <Input data-testid="employee-super-user-duration" defaultValue="20" />
        <Space>
          <Button data-testid="employee-super-user-request-submit" onClick={() => void requestElevated()} type="primary">{t('employee.superUser.request')}</Button>
          <Button data-testid="employee-super-user-approve-first" onClick={() => void approveFirst()}>{t('employee.superUser.approve')}</Button>
        </Space>
        <span>{request?.status}</span>
      </section>
    </main>
  );
}

function SectionTable<T extends object>({ titleKey, testId, columns, dataSource, rowKey }: { titleKey: string; testId: string; columns: ColumnsType<T>; dataSource: T[]; rowKey: string }): ReactElement {
  return (
    <main className="employee-page">
      <PageHeader titleKey={titleKey} subtitleKey="employee.profile.description" />
      <Table columns={columns} data-testid={testId} dataSource={dataSource} pagination={false} rowKey={rowKey} />
    </main>
  );
}

function PageHeader({ titleKey, subtitleKey }: { titleKey: string; subtitleKey: string }): ReactElement {
  return (
    <header className="employee-heading">
      <h1>{t(titleKey)}</h1>
      <span>{t(subtitleKey)}</span>
    </header>
  );
}
