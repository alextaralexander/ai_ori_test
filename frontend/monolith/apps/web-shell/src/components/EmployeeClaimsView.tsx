import { Alert, Button, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { EmployeeApiError, getEmployeeClaimDetails, getEmployeeClaims, submitEmployeeClaim, transitionEmployeeClaim, type EmployeeClaimDetailsResponse, type EmployeeClaimSummaryResponse } from '../api/employee';
import { t } from '../i18n';

interface EmployeeClaimsViewProps {
  mode: 'create' | 'history' | 'details';
  claimId?: string;
  params?: URLSearchParams;
}

export function EmployeeClaimsView({ mode, claimId, params = new URLSearchParams() }: EmployeeClaimsViewProps): ReactElement {
  const [supportReasonCode, setSupportReasonCode] = useState('CUSTOMER_CALL');
  const [items, setItems] = useState<EmployeeClaimSummaryResponse[]>([]);
  const [details, setDetails] = useState<EmployeeClaimDetailsResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const orderNumber = params.get('orderNumber') ?? 'BOG-ORD-021-001';

  useEffect(() => {
    let active = true;
    setMessageCode(null);

    async function load(): Promise<void> {
      if (mode === 'history') {
        const loaded = await getEmployeeClaims(params);
        if (active) {
          setItems(loaded.items);
        }
      }
      if (mode === 'details' && claimId) {
        const loaded = await getEmployeeClaimDetails(claimId);
        if (active) {
          setDetails(loaded);
        }
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [claimId, mode, params]);

  async function createClaim(): Promise<void> {
    try {
      setMessageCode(null);
      setDetails(await submitEmployeeClaim(orderNumber, supportReasonCode));
    } catch (error: unknown) {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_CLAIM_VALIDATION_FAILED');
    }
  }

  async function applyTransition(transitionCode: string): Promise<void> {
    if (!claimId) {
      return;
    }
    try {
      setMessageCode(null);
      setDetails(await transitionEmployeeClaim(claimId, transitionCode));
    } catch (error: unknown) {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_CLAIM_TRANSITION_INVALID');
    }
  }

  const columns: ColumnsType<EmployeeClaimSummaryResponse> = useMemo(() => [
    {
      dataIndex: 'claimNumber',
      title: t('employee.claims.column.claim'),
      render: (value: string) => <a href={`/employee/claims-history/${value}`}>{value}</a>,
    },
    { dataIndex: 'orderNumber', title: t('employee.claims.column.order') },
    { dataIndex: 'customerOrPartnerLabel', title: t('employee.claims.column.owner') },
    { dataIndex: 'slaState', title: t('employee.claims.column.sla') },
    {
      dataIndex: 'compensationAmount',
      title: t('employee.claims.column.compensation'),
      render: (_value: string, row) => `${row.compensationAmount} ${row.currencyCode}`,
    },
    { dataIndex: 'maskedContact', title: t('employee.claims.column.contact') },
  ], []);

  if (messageCode === 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED') {
    return <Alert data-testid="employee-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (messageCode) {
    return <Alert data-testid="employee-claim-error" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (mode === 'create') {
    return (
      <main className="employee-page" data-testid="employee-submit-claim-page">
        <PageHeader titleKey="employee.claims.create.title" subtitleKey="employee.claims.create.description" />
        <section className="employee-grid">
          <article className="employee-card">
            <h2>{t('employee.claims.orderContext')}</h2>
            <span>{orderNumber}</span>
            <label>
              {t('employee.claims.supportReason')}
              <input data-testid="employee-claim-support-reason" value={supportReasonCode} onChange={(event) => setSupportReasonCode(event.target.value)} />
            </label>
          </article>
          <article className="employee-card">
            <h2>{t('employee.claims.items')}</h2>
            <label>
              <input data-testid="employee-claim-item-SKU-021-001" type="checkbox" defaultChecked />
              SKU-021-001
            </label>
            <select data-testid="employee-claim-problem-type" defaultValue="DAMAGED_ITEM">
              <option value="DAMAGED_ITEM">{t('employee.claims.problem.damaged')}</option>
            </select>
            <select data-testid="employee-claim-requested-resolution" defaultValue="REFUND">
              <option value="REFUND">{t('employee.claims.resolution.refund')}</option>
            </select>
          </article>
          <article className="employee-card" data-testid="employee-claim-compensation-preview">
            <h2>{t('employee.claims.compensationPreview')}</h2>
            <strong>1250 RUB AT_RISK</strong>
          </article>
        </section>
        <Button data-testid="employee-claim-submit" type="primary" onClick={() => void createClaim()}>{t('employee.claims.submit')}</Button>
        {details ? <ClaimResult details={details} /> : null}
      </main>
    );
  }

  if (mode === 'details') {
    return (
      <main className="employee-page" data-testid="employee-claim-details-page">
        <PageHeader titleKey="employee.claims.details.title" subtitleKey="employee.claims.details.description" />
        <section className="employee-grid">
          <article className="employee-card" data-testid="employee-claim-items">
            <h2>{t('employee.claims.items')}</h2>
            {details?.items.map((item) => <div key={item.sku}>{item.sku} {item.problemType} {item.compensationAmount}</div>)}
          </article>
          <article className="employee-card" data-testid="employee-claim-route-tasks">
            <h2>{t('employee.claims.routeTasks')}</h2>
            {details?.routeTasks.map((task) => <div key={task.taskId}>{task.taskType} {task.status} {task.resultCode}</div>)}
          </article>
          <article className="employee-card" data-testid="employee-claim-audit">
            <h2>{t('employee.claims.audit')}</h2>
            {details?.auditEvents.map((event) => <div key={event.auditEventId}>{event.actionType} {event.actorRole}</div>)}
          </article>
        </section>
        <Space wrap>
          <Button data-testid="employee-claim-transition-finance" onClick={() => void applyTransition('SEND_TO_FINANCE_REFUND')}>{t('employee.claims.transition.finance')}</Button>
          <Button data-testid="employee-claim-approve-compensation" onClick={() => void applyTransition('APPROVE_COMPENSATION')}>{t('employee.claims.transition.approve')}</Button>
        </Space>
      </main>
    );
  }

  return (
    <main className="employee-page" data-testid="employee-claims-history-page">
      <PageHeader titleKey="employee.claims.history.title" subtitleKey="employee.claims.history.description" />
      <section className="employee-search" data-testid="employee-claims-filters">
        <span>{t('employee.claims.filters')}</span>
        <span>{params.toString()}</span>
      </section>
      <Table columns={columns} data-testid="employee-claims-table" dataSource={items} pagination={false} rowKey="claimId" />
    </main>
  );
}

function ClaimResult({ details }: { details: EmployeeClaimDetailsResponse }): ReactElement {
  return (
    <section className="employee-card" data-testid="employee-claim-result">
      <span>{details.claimNumber}</span>
      <span>{details.slaState}</span>
      <span>{details.publicReasonMnemonic}</span>
    </section>
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
