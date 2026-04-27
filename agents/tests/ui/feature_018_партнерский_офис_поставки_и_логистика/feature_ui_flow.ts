import { expect, test } from '@playwright/test';

test('partner office opens all orders list', async ({ page }) => {
  await page.goto('/test-login?role=partner-office');
  await expect(page.getByTestId('session-ready')).toContainText('partner-office');
  await page.goto('/partner-office/all-orders');
  await expect(page.getByTestId('partner-office-orders-page')).toBeVisible();
  await expect(page.getByTestId('partner-office-order-card-BOG-ORD-018-001')).toContainText('BOG-ORD-018-001');
  await expect(page.getByTestId('partner-office-order-card-BOG-ORD-018-001')).toContainText('BOG-SUP-018-001');
});

test('partner office filters deviation orders', async ({ page }) => {
  await page.goto('/test-login?role=partner-office');
  await expect(page.getByTestId('session-ready')).toContainText('partner-office');
  await page.goto('/partner-office/all-orders?supplyId=BOG-SUP-018-001&hasDeviation=true');
  await expect(page.getByTestId('partner-office-orders-page')).toBeVisible();
  await expect(page.getByTestId('partner-office-order-card-BOG-ORD-018-002')).toContainText(/BOG-ORD-018-002|SHORTAGE|недостача/i);
});

test('partner office opens supply list and details', async ({ page }) => {
  await page.goto('/test-login?role=partner-office');
  await expect(page.getByTestId('session-ready')).toContainText('partner-office');
  await page.goto('/partner-office/supply');
  await expect(page.getByTestId('partner-office-supply-page')).toBeVisible();
  await expect(page.getByTestId('partner-office-supply-card-BOG-SUP-018-001')).toContainText('BOG-SUP-018-001');
  await page.goto('/partner-office/supply/BOG-SUP-018-001');
  await expect(page.getByTestId('partner-office-supply-details-page')).toBeVisible();
  await expect(page.getByTestId('partner-office-supply-movements')).toBeVisible();
  await expect(page.getByTestId('partner-office-supply-actions')).toBeVisible();
});

test('partner office opens order inside supply and records deviation', async ({ page }) => {
  await page.goto('/test-login?role=partner-office');
  await expect(page.getByTestId('session-ready')).toContainText('partner-office');
  await page.goto('/partner-office/supply/orders/BOG-ORD-018-002');
  await expect(page.getByTestId('partner-office-supply-order-details-page')).toBeVisible();
  await expect(page.getByTestId('partner-office-supply-order-items')).toContainText('SKU-018-LIP-001');
  await page.getByTestId('partner-office-record-deviation').click();
  await expect(page.getByTestId('partner-office-action-result')).toContainText('STR_MNEMO_PARTNER_OFFICE_DEVIATION_RECORDED');
});

test('regional manager opens office report', async ({ page }) => {
  await page.goto('/test-login?role=regional-manager');
  await expect(page.getByTestId('session-ready')).toContainText('regional-manager');
  await page.goto('/partner-office/report');
  await expect(page.getByTestId('partner-office-report-page')).toBeVisible();
  await expect(page.getByTestId('partner-office-report-kpi')).toContainText(/SLA|приемк|acceptance/i);
});

test('foreign office cannot open supply', async ({ page }) => {
  await page.goto('/test-login?role=partner-office-foreign');
  await expect(page.getByTestId('session-ready')).toContainText('partner-office-foreign');
  await page.goto('/partner-office/supply/BOG-SUP-018-001');
  await expect(page.getByTestId('partner-office-access-denied')).toContainText(/STR_MNEMO_PARTNER_OFFICE_ACCESS_DENIED|доступ|access/i);
});
