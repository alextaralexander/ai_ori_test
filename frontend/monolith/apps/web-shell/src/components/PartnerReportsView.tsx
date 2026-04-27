import { Alert, Button, Input } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { downloadPartnerReportDocument, exportPartnerReport, getPartnerCommissionDetails, getPartnerFinanceReconciliation, getPartnerReportDocuments, getPartnerReportPrintView, getPartnerReportSummary, PartnerReportApiError, revokePartnerReportDocument, searchPartnerReportOrders, type PartnerCommissionDetailResponse, type PartnerReportDocumentDownloadResponse, type PartnerReportDocumentPageResponse, type PartnerReportDocumentResponse, type PartnerReportFinanceReconciliationResponse, type PartnerReportOrderPageResponse, type PartnerReportPrintViewResponse, type PartnerReportSummaryResponse } from '../api/partnerReporting';
import { t } from '../i18n';

interface PartnerReportsViewProps {
  mode: 'orders' | 'documents';
  params?: URLSearchParams;
}

const revokedDocumentId = '00000000-0015-0000-0000-000000000003';

export function PartnerReportsView({ mode, params = new URLSearchParams() }: PartnerReportsViewProps): ReactElement {
  const [summary, setSummary] = useState<PartnerReportSummaryResponse | null>(null);
  const [orders, setOrders] = useState<PartnerReportOrderPageResponse | null>(null);
  const [documents, setDocuments] = useState<PartnerReportDocumentPageResponse | null>(null);
  const [details, setDetails] = useState<PartnerCommissionDetailResponse | null>(null);
  const [download, setDownload] = useState<PartnerReportDocumentDownloadResponse | null>(null);
  const [printView, setPrintView] = useState<PartnerReportPrintViewResponse | null>(null);
  const [finance, setFinance] = useState<PartnerReportFinanceReconciliationResponse | null>(null);
  const [revokedDocument, setRevokedDocument] = useState<PartnerReportDocumentResponse | null>(null);
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [exportMessage, setExportMessage] = useState<string | null>(null);
  const [orderNumber, setOrderNumber] = useState(params.get('orderNumber') ?? '');

  useEffect(() => {
    let active = true;
    async function load(): Promise<void> {
      setMessageCode(null);
      const financePartnerId = params.get('financePartnerId');
      if (financePartnerId) {
        const loadedFinance = await getPartnerFinanceReconciliation(financePartnerId);
        if (active) {
          setFinance(loadedFinance);
        }
        return;
      }
      const nextParams = new URLSearchParams(params);
      if (!nextParams.has('catalogId')) {
        nextParams.set('catalogId', 'CAT-2026-05');
      }
      const loadedSummary = await getPartnerReportSummary(nextParams);
      const loadedOrders = await searchPartnerReportOrders(nextParams);
      const loadedDocuments = await getPartnerReportDocuments(nextParams);
      if (active) {
        setSummary(loadedSummary);
        setOrders(loadedOrders);
        setDocuments(loadedDocuments);
      }
    }
    void load().catch((error: unknown) => {
      setMessageCode(error instanceof PartnerReportApiError ? error.code : 'STR_MNEMO_PARTNER_REPORT_ACCESS_DENIED');
    });
    return () => {
      active = false;
    };
  }, [params]);

  async function submitFilters(): Promise<void> {
    const next = new URLSearchParams(params);
    if (orderNumber) {
      next.set('orderNumber', orderNumber);
    } else {
      next.delete('orderNumber');
    }
    setOrders(await searchPartnerReportOrders(next));
  }

  async function openCommission(order: string): Promise<void> {
    setDetails(await getPartnerCommissionDetails(order));
  }

  async function downloadDocument(documentId: string): Promise<void> {
    setDownload(await downloadPartnerReportDocument(documentId));
  }

  async function openPrintView(documentId: string): Promise<void> {
    setPrintView(await getPartnerReportPrintView(documentId));
  }

  async function runExport(): Promise<void> {
    const response = await exportPartnerReport();
    setExportMessage(response.publicMnemo);
  }

  async function revokeDocument(): Promise<void> {
    setRevokedDocument(await revokePartnerReportDocument(revokedDocumentId));
  }

  if (messageCode === 'STR_MNEMO_PARTNER_REPORT_ACCESS_DENIED') {
    return <Alert data-testid="partner-report-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  if (finance) {
    return (
      <main className="partner-report-page" data-testid="partner-report-page">
        <header className="partner-report-heading">
          <h1>{t('partnerReports.finance.title')}</h1>
          <span data-testid="partner-report-reconciliation-status">{finance.reconciliationStatus}</span>
        </header>
        <Alert data-testid="partner-report-reconciliation-message" message={`${t(finance.publicMnemo ?? 'STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH')} (${finance.publicMnemo})`} type="warning" />
        <section className="partner-report-totals">
          <Metric testId="partner-report-total-payable" titleKey="partnerReports.total.payable" value={finance.totals.payable} />
          <Metric testId="partner-report-total-paid" titleKey="partnerReports.total.paid" value={finance.totals.paid} />
        </section>
        <Button data-testid="partner-report-revoke-DOC-015-ACT-003" onClick={() => void revokeDocument()}>{t('partnerReports.document.revoke')}</Button>
        {revokedDocument ? <article data-testid="partner-report-document-DOC-015-ACT-003">{revokedDocument.documentCode} · {revokedDocument.documentStatus}</article> : null}
      </main>
    );
  }

  return (
    <main className="partner-report-page" data-testid={mode === 'documents' ? 'partner-report-documents-page' : 'partner-report-page'}>
      <header className="partner-report-heading">
        <h1>{t(mode === 'documents' ? 'partnerReports.documents.title' : 'partnerReports.title')}</h1>
        <span>{t('partnerReports.description')}</span>
      </header>
      {messageCode ? <Alert data-testid="partner-report-message" message={`${t(messageCode)} (${messageCode})`} type="warning" /> : null}
      {summary ? (
        <section className="partner-report-totals">
          <Metric testId="partner-report-total-gross-sales" titleKey="partnerReports.total.grossSales" value={summary.totals.grossSales} />
          <Metric testId="partner-report-total-accrued-commission" titleKey="partnerReports.total.accruedCommission" value={summary.totals.accruedCommission} />
          <Metric testId="partner-report-total-withheld" titleKey="partnerReports.total.withheld" value={summary.totals.withheld} />
          <Metric testId="partner-report-total-payable" titleKey="partnerReports.total.payable" value={summary.totals.payable} />
        </section>
      ) : null}
      <section className="partner-report-filters">
        <Input data-testid="partner-report-order-filter" onChange={(event) => setOrderNumber(event.target.value)} placeholder={t('partnerReports.filter.order')} value={orderNumber} />
        <Button data-testid="partner-report-filter-submit" onClick={() => void submitFilters()} type="primary">{t('partnerReports.filter.submit')}</Button>
        <Button data-testid="partner-report-export-xlsx" onClick={() => void runExport()}>{t('partnerReports.export.xlsx')}</Button>
      </section>
      {exportMessage ? <Alert data-testid="partner-report-export-result" message={`${t(exportMessage)} (${exportMessage})`} type="success" /> : null}
      {mode === 'documents' ? (
        <DocumentList documents={documents} onDownload={downloadDocument} onPrint={openPrintView} />
      ) : (
        <OrderList onOpen={openCommission} orders={orders} />
      )}
      {details ? (
        <section className="partner-report-details" data-testid="partner-commission-details">
          <h2>{details.orderLine.orderNumber}</h2>
          <div>{details.payoutReference}</div>
          <div>{details.correlationId}</div>
          <div data-testid="partner-commission-adjustments">
            {details.adjustments.map((adjustment) => <span key={`${adjustment.adjustmentType}-${adjustment.sourceRef}`}>{adjustment.adjustmentType} {adjustment.amount.amount}</span>)}
          </div>
        </section>
      ) : null}
      {download ? <Alert data-testid="partner-report-document-download-result" message={`${download.documentCode} ${download.checksumSha256}`} type="success" /> : null}
      {printView ? <section data-testid="partner-report-print-view">{printView.documentCode} · {printView.checksumSha256}</section> : null}
    </main>
  );
}

function Metric({ testId, titleKey, value }: { testId: string; titleKey: string; value: { amount: number; currencyCode: string } }): ReactElement {
  return (
    <article className="partner-report-metric" data-testid={testId}>
      <span>{t(titleKey)}</span>
      <strong>{value.amount} {value.currencyCode}</strong>
    </article>
  );
}

function OrderList({ onOpen, orders }: { onOpen: (orderNumber: string) => Promise<void>; orders: PartnerReportOrderPageResponse | null }): ReactElement {
  return (
    <section className="partner-report-list">
      {orders?.items.map((order) => (
        <article className="partner-report-row" data-testid={`partner-report-order-${order.orderNumber}`} key={order.orderNumber}>
          <strong>{order.orderNumber}</strong>
          <span>{order.commissionBase.amount} {order.commissionBase.currencyCode}</span>
          <span>{order.calculationStatus}</span>
          <Button data-testid={`partner-report-open-commission-${order.orderNumber}`} onClick={() => void onOpen(order.orderNumber)}>{t('partnerReports.details')}</Button>
        </article>
      ))}
    </section>
  );
}

function DocumentList({ documents, onDownload, onPrint }: { documents: PartnerReportDocumentPageResponse | null; onDownload: (documentId: string) => Promise<void>; onPrint: (documentId: string) => Promise<void> }): ReactElement {
  return (
    <section className="partner-report-list">
      {documents?.items.map((document) => (
        <article className="partner-report-row" data-testid={`partner-report-document-${document.documentCode}`} key={document.documentId}>
          <strong>{document.documentCode}</strong>
          <span>{document.documentType}</span>
          <span>{document.documentStatus}</span>
          <span>{document.checksumSha256}</span>
          <Button data-testid={`partner-report-download-${document.documentCode}`} onClick={() => void onDownload(document.documentId)}>{t('partnerReports.document.download')}</Button>
          <Button data-testid={`partner-report-print-${document.documentCode}`} onClick={() => void onPrint(document.documentId)}>{t('partnerReports.document.print')}</Button>
        </article>
      ))}
    </section>
  );
}
