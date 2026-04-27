import { Alert, Button, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { EmployeeApiError, getEmployeeOrderHistory, getEmployeeOrderHistoryDetails, type EmployeeOrderHistoryDetailsResponse, type EmployeeOrderHistorySummaryResponse } from '../api/employee';
import { t } from '../i18n';

interface EmployeeOrderHistoryViewProps {
  orderId?: string;
  params?: URLSearchParams;
}

export function EmployeeOrderHistoryView({ orderId, params = new URLSearchParams() }: EmployeeOrderHistoryViewProps): ReactElement {
  const [items, setItems] = useState<EmployeeOrderHistorySummaryResponse[]>([]);
  const [details, setDetails] = useState<EmployeeOrderHistoryDetailsResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setMessageCode(null);

    async function load(): Promise<void> {
      if (orderId) {
        const loaded = await getEmployeeOrderHistoryDetails(orderId);
        if (active) {
          setDetails(loaded);
        }
        return;
      }
      const loaded = await getEmployeeOrderHistory(params);
      if (active) {
        setItems(loaded.items);
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [orderId, params]);

  const columns: ColumnsType<EmployeeOrderHistorySummaryResponse> = useMemo(() => [
    {
      dataIndex: 'orderNumber',
      title: t('employee.orderHistory.column.order'),
      render: (value: string) => <a href={`/employee/order-history/${value}`}>{value}</a>,
    },
    {
      dataIndex: 'partnerDisplayName',
      title: t('employee.orderHistory.column.partner'),
    },
    {
      dataIndex: 'customerDisplayName',
      title: t('employee.orderHistory.column.customer'),
      render: (_value: string, row) => `${row.customerDisplayName} ${row.maskedPhone}`,
    },
    {
      dataIndex: 'fulfillmentStatus',
      title: t('employee.orderHistory.column.status'),
      render: (_value: string, row) => `${row.orderStatus} ${row.paymentStatus} ${row.deliveryStatus} ${row.fulfillmentStatus}`,
    },
    {
      dataIndex: 'totalAmount',
      title: t('employee.orderHistory.column.total'),
      render: (_value: string, row) => `${row.totalAmount} ${row.currencyCode}`,
    },
    {
      dataIndex: 'problemFlags',
      title: t('employee.orderHistory.column.problems'),
      render: (value: string[]) => value.join(' '),
    },
  ], []);

  if (messageCode === 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED') {
    return <Alert data-testid="employee-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (messageCode) {
    return <Alert data-testid="employee-order-history-error" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (orderId) {
    return (
      <main className="employee-page" data-testid="employee-order-history-details-page">
        <PageHeader titleKey="employee.orderHistory.details.title" subtitleKey="employee.orderHistory.details.description" />
        <section className="employee-grid">
          <article className="employee-card" data-testid="employee-order-details-summary">
            <h2>{details?.orderNumber}</h2>
            <span>{details?.customerDisplayName} {details?.maskedPhone}</span>
            <span>{details?.partnerDisplayName}</span>
            <span>{details?.orderStatus} {details?.paymentStatus} {details?.deliveryStatus} {details?.fulfillmentStatus}</span>
            <strong>{details?.totalAmount} {details?.currencyCode}</strong>
          </article>
          <article className="employee-card" data-testid="employee-order-details-items">
            <h2>{t('employee.orderHistory.items')}</h2>
            {details?.items.map((item) => <div key={item.sku}>{item.sku} {item.productName} {item.quantity} {item.reserveStatus}</div>)}
          </article>
          <article className="employee-card" data-testid="employee-order-details-events">
            <h2>{t('employee.orderHistory.events')}</h2>
            {[...(details?.paymentEvents ?? []), ...(details?.deliveryEvents ?? []), ...(details?.wmsEvents ?? [])].map((event) => (
              <div key={event.eventId}>{event.eventType} {event.status} {event.messageCode}</div>
            ))}
          </article>
          <article className="employee-card" data-testid="employee-order-details-audit">
            <h2>{t('employee.orderHistory.audit')}</h2>
            {details?.auditEvents.map((event) => <div key={`${event.eventType}-${event.occurredAt}`}>{event.eventType} {event.actorUserId} actorRole {event.actorRole} {details.supervisorRequired ? 'supervisorRequired' : ''}</div>)}
          </article>
        </section>
        <Space wrap>
          <Button data-testid="employee-order-support-link" href={details?.linkedRoutes.support ?? '/employee/order-support?orderNumber=BOG-ORD-020-001'}>{t('employee.orderHistory.support')}</Button>
          <Button href={details?.linkedRoutes.claim}>{t('employee.orderHistory.claim')}</Button>
          <Button href={details?.linkedRoutes.paymentEvents}>{t('employee.orderHistory.paymentEvents')}</Button>
        </Space>
      </main>
    );
  }

  return (
    <main className="employee-page" data-testid="employee-order-history-page">
      <PageHeader titleKey="employee.orderHistory.title" subtitleKey="employee.orderHistory.description" />
      <section className="employee-search" data-testid="employee-order-history-filters">
        <span>{t('employee.orderHistory.filters')}</span>
        <span>{params.toString()}</span>
      </section>
      <Table
        columns={columns}
        data-testid="employee-order-history-table"
        dataSource={items}
        pagination={false}
        rowKey="orderId"
      />
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
