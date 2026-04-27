import { expect, test } from '@playwright/test';

test('partner opens report summary and commission order lines', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/report/order-history?catalogId=CAT-2026-05');
  await expect(page.getByTestId('partner-report-page')).toBeVisible();
  await expect(page.getByTestId('partner-report-total-gross-sales')).toBeVisible();
  await expect(page.getByTestId('partner-report-total-accrued-commission')).toBeVisible();
  await expect(page.getByTestId('partner-report-order-ORD-015-STRUCTURE-001')).toBeVisible();
});

test('partner opens commission details with payout reference', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/report/order-history?orderNumber=ORD-015-STRUCTURE-001');
  await page.getByTestId('partner-report-open-commission-ORD-015-STRUCTURE-001').click();
  await expect(page.getByTestId('partner-commission-details')).toContainText('ORD-015-STRUCTURE-001');
  await expect(page.getByTestId('partner-commission-details')).toContainText(/payout|выплат/i);
  await expect(page.getByTestId('partner-commission-adjustments')).toBeVisible();
});

test('partner downloads document and opens print view', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/report/info-reciept');
  await expect(page.getByTestId('partner-report-documents-page')).toBeVisible();
  await expect(page.getByTestId('partner-report-document-DOC-015-ACT-001')).toContainText(/PUBLISHED|опублик/i);
  await page.getByTestId('partner-report-download-DOC-015-ACT-001').click();
  await expect(page.getByTestId('partner-report-document-download-result')).toContainText(/checksum|sha256/i);
  await page.getByTestId('partner-report-print-DOC-015-ACT-001').click();
  await expect(page.getByTestId('partner-report-print-view')).toContainText(/DOC-015-ACT-001|checksum/i);
});

test('partner exports report in xlsx format', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/report/order-history');
  await page.getByTestId('partner-report-export-xlsx').click();
  await expect(page.getByTestId('partner-report-export-result')).toContainText(/STR_MNEMO_PARTNER_REPORT_EXPORT_READY|экспорт/i);
});

test('finance controller can see mismatch and revoke document', async ({ page }) => {
  await page.goto('/test-login?role=finance-controller');
  await expect(page.getByTestId('session-ready')).toContainText('finance-controller');
  await page.goto('/report/order-history?financePartnerId=partner-015');
  await expect(page.getByTestId('partner-report-reconciliation-status')).toContainText(/MISMATCH|расхожд/i);
  await expect(page.getByTestId('partner-report-reconciliation-message')).toContainText(/STR_MNEMO_PARTNER_REPORT_RECONCILIATION_MISMATCH|расхожд/i);
  await page.getByTestId('partner-report-revoke-DOC-015-ACT-003').click();
  await expect(page.getByTestId('partner-report-document-DOC-015-ACT-003')).toContainText(/REVOKED|отозван/i);
});

test('partner cannot open foreign finance view', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/report/order-history?financePartnerId=partner-foreign');
  await expect(page.getByTestId('partner-report-access-denied')).toContainText(/STR_MNEMO_PARTNER_REPORT_ACCESS_DENIED|access|доступ/i);
});
