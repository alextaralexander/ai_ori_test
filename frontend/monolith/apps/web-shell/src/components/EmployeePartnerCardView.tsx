import { Alert, Button, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { HTMLAttributes, ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { EmployeeApiError, getEmployeePartnerCard, getEmployeePartnerOrderReport, type EmployeePartnerCardResponse, type EmployeePartnerOrderReportResponse, type EmployeePartnerOrderSummaryResponse } from '../api/employee';
import { t } from '../i18n';

interface EmployeePartnerCardViewProps {
  mode: 'card' | 'report';
  params?: URLSearchParams;
}

export function EmployeePartnerCardView({ mode, params = new URLSearchParams() }: EmployeePartnerCardViewProps): ReactElement {
  const [card, setCard] = useState<EmployeePartnerCardResponse | null>(null);
  const [report, setReport] = useState<EmployeePartnerOrderReportResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const query = params.get('query') ?? params.get('personNumber') ?? 'P-022-7788';

  useEffect(() => {
    let active = true;
    setMessageCode(null);

    async function load(): Promise<void> {
      if (mode === 'card') {
        const loaded = await getEmployeePartnerCard(query);
        if (active) {
          setCard(loaded);
        }
        return;
      }
      const loaded = await getEmployeePartnerOrderReport(params);
      if (active) {
        setReport(loaded);
      }
    }

    void load().catch((error: unknown) => {
      setMessageCode(error instanceof EmployeeApiError ? error.code : 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [mode, params, query]);

  const columns: ColumnsType<EmployeePartnerOrderSummaryResponse> = useMemo(() => [
    {
      dataIndex: 'orderNumber',
      title: t('employee.partnerReport.column.order'),
      render: (value: string, row) => <a href={row.linkedRoutes.details}>{value}</a>,
    },
    { dataIndex: 'campaignCode', title: t('employee.partnerReport.column.campaign') },
    { dataIndex: 'customerDisplayName', title: t('employee.partnerReport.column.customer') },
    {
      dataIndex: 'fulfillmentStatus',
      title: t('employee.partnerReport.column.status'),
      render: (_value: string, row) => `${row.orderStatus} ${row.paymentStatus} ${row.deliveryStatus} ${row.fulfillmentStatus}`,
    },
    {
      dataIndex: 'totalAmount',
      title: t('employee.partnerReport.column.total'),
      render: (_value: string, row) => `${row.totalAmount} ${row.currencyCode}`,
    },
    { dataIndex: 'bonusVolume', title: t('employee.partnerReport.column.bonusVolume') },
    {
      dataIndex: 'problemFlags',
      title: t('employee.partnerReport.column.problems'),
      render: (value: string[]) => value.join(' '),
    },
  ], []);

  if (messageCode === 'STR_MNEMO_EMPLOYEE_ACCESS_DENIED') {
    return <Alert data-testid="employee-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (messageCode) {
    return <Alert data-testid="employee-partner-card-error" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (mode === 'report') {
    return (
      <main className="employee-page" data-testid="employee-partner-report-page">
        <PageHeader titleKey="employee.partnerReport.title" subtitleKey="employee.partnerReport.description" />
        <section className="employee-search" data-testid="employee-partner-report-filters">
          <span>{t('employee.partnerReport.filters')}</span>
          <span>{params.toString()}</span>
        </section>
        <section className="employee-card" data-testid="employee-partner-report-aggregate">
          <strong>{t('employee.partnerReport.aggregate.totalOrders')}: {report?.aggregates.totalOrders}</strong>
          <span>{t('employee.partnerReport.aggregate.totalAmount')}: {report?.aggregates.totalAmount} {report?.aggregates.currencyCode}</span>
          <span>{t('employee.partnerReport.aggregate.personalVolume')}: {report?.aggregates.personalVolume}</span>
          <span>{t('employee.partnerReport.aggregate.groupVolume')}: {report?.aggregates.groupVolume}</span>
          <span>{t('employee.partnerReport.aggregate.openClaimCount')}: {report?.aggregates.openClaimCount}</span>
        </section>
        <Table
          columns={columns}
          data-testid="employee-partner-report-table"
          dataSource={report?.items ?? []}
          pagination={false}
          rowKey="orderId"
        />
      </main>
    );
  }

  return (
    <main className="employee-page" data-testid="employee-partner-card-page">
      <PageHeader titleKey="employee.partnerCard.title" subtitleKey="employee.partnerCard.description" />
      <section className="employee-search" data-testid="employee-partner-card-search">
        <span>{t('employee.partnerCard.search')}</span>
        <strong>{query}</strong>
      </section>
      <section className="employee-grid">
        <article className="employee-card" data-testid="employee-partner-card-summary">
          <h2>{card?.displayName}</h2>
          <span>{card?.personNumber} {card?.status} {card?.activityState}</span>
          <span>{card?.levelName} {card?.regionCode} {card?.mentorPersonNumber}</span>
          <span>{card?.maskedPhone} {card?.maskedEmail}</span>
        </article>
        <article className="employee-card" data-testid="employee-partner-card-kpi">
          <h2>{t('employee.partnerCard.kpi.title')}</h2>
          <span>{t('employee.partnerCard.kpi.personalVolume')}: {card?.kpi.personalVolume}</span>
          <span>{t('employee.partnerCard.kpi.groupVolume')}: {card?.kpi.groupVolume}</span>
          <span>{t('employee.partnerCard.kpi.orderCount')}: {card?.kpi.orderCount}</span>
          <span>{t('employee.partnerCard.kpi.bonusBalance')}: {card?.kpi.bonusBalance}</span>
        </article>
      </section>
      <Table
        columns={columns}
        dataSource={card?.recentOrders ?? []}
        pagination={false}
        rowKey="orderId"
        rowClassName={(record) => `employee-partner-order-${record.orderNumber}`}
        onRow={(record) => ({ 'data-testid': `employee-partner-order-row-${record.orderNumber}` } as HTMLAttributes<HTMLElement>)}
      />
      <Space wrap>
        <Button href={card?.linkedRoutes.orderHistory}>{t('employee.partnerCard.link.report')}</Button>
        <Button href={card?.linkedRoutes.support}>{t('employee.partnerCard.link.support')}</Button>
        <Button href={card?.linkedRoutes.claim}>{t('employee.partnerCard.link.claim')}</Button>
      </Space>
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
