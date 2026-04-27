import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { addEmployeeNote, createEmployeeOperatorOrder, EmployeeApiError, getEmployeeEscalations, getEmployeeOrderSupport, getEmployeeWorkspace, recordEmployeeAdjustment, type EmployeeEscalationPageResponse, type EmployeeOperatorOrderResponse, type EmployeeOrderSupportResponse, type EmployeeSupportActionResponse, type EmployeeWorkspaceResponse } from '../api/employee';
import { t } from '../i18n';

interface EmployeeWorkspaceViewProps {
  mode: 'workspace' | 'new-order' | 'order-support';
  params?: URLSearchParams;
}

export function EmployeeWorkspaceView({ mode, params = new URLSearchParams() }: EmployeeWorkspaceViewProps): ReactElement {
  const [query, setQuery] = useState(params.get('query') ?? params.get('customerId') ?? 'CUST-019-001');
  const [workspace, setWorkspace] = useState<EmployeeWorkspaceResponse | null>(null);
  const [operatorOrder, setOperatorOrder] = useState<EmployeeOperatorOrderResponse | null>(null);
  const [support, setSupport] = useState<EmployeeOrderSupportResponse | null>(null);
  const [action, setAction] = useState<EmployeeSupportActionResponse | null>(null);
  const [escalations, setEscalations] = useState<EmployeeEscalationPageResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setMessageCode(null);
    setAction(null);

    async function load(): Promise<void> {
      if (mode === 'workspace') {
        const loaded = await getEmployeeWorkspace(query || 'CUST-019-001');
        if (active) setWorkspace(loaded);
      } else if (mode === 'new-order') {
        const loaded = await getEmployeeWorkspace(params.get('customerId') ?? 'CUST-019-001');
        if (active) setWorkspace(loaded);
      } else if (params.get('view') === 'supervisor') {
        const loaded = await getEmployeeEscalations();
        if (active) setEscalations(loaded);
      } else {
        const loaded = await getEmployeeOrderSupport(params.get('orderNumber') ?? 'BOG-ORD-019-001');
        if (active) setSupport(loaded);
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [mode, params, query]);

  async function search(): Promise<void> {
    setWorkspace(await getEmployeeWorkspace(query || 'CUST-019-001'));
  }

  async function createOrder(): Promise<void> {
    const result = await createEmployeeOperatorOrder(workspace?.customer.customerId ?? 'CUST-019-001');
    setOperatorOrder(result);
    setMessageCode(result.messageCode);
  }

  async function addNote(): Promise<void> {
    const result = await addEmployeeNote(support?.orderNumber ?? 'BOG-ORD-019-001');
    setAction(result);
    setMessageCode(result.messageCode);
  }

  async function recordAdjustment(): Promise<void> {
    const result = await recordEmployeeAdjustment(support?.orderNumber ?? 'BOG-ORD-019-001');
    setAction(result);
    setMessageCode(result.messageCode);
  }

  if (messageCode === 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED') {
    return <Alert data-testid="employee-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (mode === 'new-order') {
    return (
      <main className="employee-page" data-testid="employee-new-order-page">
        <PageHeader titleKey="employee.newOrder.title" subtitleKey="employee.newOrder.description" />
        {messageCode ? <Alert data-testid="employee-action-result" message={`${t(messageCode)} (${messageCode})`} type="success" /> : null}
        <section className="employee-grid">
          <CustomerCard workspace={workspace} />
          <article className="employee-card" data-testid="employee-operator-order-card">
            <h2>{t('employee.newOrder.items')}</h2>
            {workspace?.activeCart.items.map((item) => <div key={item.sku}>{item.sku} · {item.productName} · {item.quantity}</div>)}
            <strong>{workspace?.activeCart.subtotalAmount} {workspace?.activeCart.currencyCode}</strong>
            {operatorOrder ? <span>{operatorOrder.orderNumber} · {operatorOrder.paymentStatus} · {operatorOrder.deliveryStatus}</span> : null}
            <Button data-testid="employee-create-operator-order" onClick={() => void createOrder()} type="primary">{t('employee.newOrder.create')}</Button>
          </article>
        </section>
      </main>
    );
  }

  if (mode === 'order-support') {
    if (params.get('view') === 'supervisor') {
      return (
        <main className="employee-page" data-testid="employee-order-support-page">
          <PageHeader titleKey="employee.supervisor.title" subtitleKey="employee.supervisor.description" />
          <section className="employee-list" data-testid="employee-supervisor-escalations">
            {escalations?.items.map((item) => <ActionCard action={item} key={item.actionId} />)}
          </section>
        </main>
      );
    }

    return (
      <main className="employee-page" data-testid="employee-order-support-page">
        <PageHeader titleKey="employee.support.title" subtitleKey="employee.support.description" />
        {messageCode ? <Alert data-testid="employee-action-result" message={`${t(messageCode)} (${messageCode})`} type="success" /> : null}
        <section className="employee-grid">
          <article className="employee-card" data-testid="employee-order-timeline">
            <h2>{support?.orderNumber}</h2>
            {support?.timeline.map((event) => <div key={`${event.eventType}-${event.occurredAt}`}>{support.orderNumber} · {event.publicStatus} · {event.descriptionCode}</div>)}
          </article>
          <article className="employee-card">
            <h2>{t('employee.support.actions')}</h2>
            {action ? <ActionCard action={action} /> : null}
            <Space wrap>
              <Button data-testid="employee-add-note" onClick={() => void addNote()}>{t('employee.support.note')}</Button>
              <Button data-testid="employee-record-adjustment" onClick={() => void recordAdjustment()} type="primary">{t('employee.support.adjustment')}</Button>
            </Space>
          </article>
        </section>
      </main>
    );
  }

  return (
    <main className="employee-page" data-testid="employee-workspace-page">
      <PageHeader titleKey="employee.workspace.title" subtitleKey="employee.workspace.description" />
      <section className="employee-search">
        <Input data-testid="employee-search-query" onChange={(event) => setQuery(event.target.value)} placeholder={t('employee.search.placeholder')} value={query} />
        <Button data-testid="employee-search-submit" onClick={() => void search()} type="primary">{t('employee.search.submit')}</Button>
      </section>
      <section className="employee-grid">
        <CustomerCard workspace={workspace} />
        <article className="employee-card" data-testid="employee-active-cart">
          <h2>{t('employee.workspace.cart')}</h2>
          {workspace?.activeCart.items.map((item) => <div key={item.sku}>{item.sku} · {item.productName} · {item.quantity}</div>)}
          <a href={`/employee/new-order?customerId=${workspace?.customer.customerId ?? 'CUST-019-001'}`}>{t('employee.workspace.newOrder')}</a>
        </article>
        <article className="employee-card">
          <h2>{t('employee.workspace.orders')}</h2>
          {workspace?.recentOrders.map((order) => <a href={`/employee/order-support?orderNumber=${order.orderNumber}`} key={order.orderNumber}>{order.orderNumber} · {order.deliveryStatus}</a>)}
        </article>
      </section>
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

function CustomerCard({ workspace }: { workspace: EmployeeWorkspaceResponse | null }): ReactElement {
  return (
    <article className="employee-card" data-testid="employee-customer-card">
      <h2>{workspace?.customer.displayName ?? t('employee.workspace.customer')}</h2>
      <span>{workspace?.customer.customerId}</span>
      <span>{workspace?.customer.partnerPersonNumber}</span>
      <span>{workspace?.customer.maskedPhone} · {workspace?.customer.maskedEmail}</span>
      <span>{workspace?.auditContext.supportReasonCode}</span>
    </article>
  );
}

function ActionCard({ action }: { action: EmployeeSupportActionResponse }): ReactElement {
  return (
    <article className="employee-action-card">
      <strong>{action.orderNumber}</strong>
      <span>{action.actionType} · {action.reasonCode}</span>
      <span>{action.messageCode}</span>
      <span>{action.supervisorRequired ? t('employee.support.supervisorRequired') : t('employee.support.supervisorNotRequired')}</span>
    </article>
  );
}
