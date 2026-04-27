import { expect, test } from '@playwright/test';

test('employee opens order history and filters problem orders', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/order-history?partnerId=PART-020-001&problemOnly=true');
  await expect(page.getByTestId('employee-order-history-page')).toBeVisible();
  await expect(page.getByTestId('employee-order-history-filters')).toBeVisible();
  await expect(page.getByTestId('employee-order-history-table')).toContainText('BOG-ORD-020-001');
  await expect(page.getByTestId('employee-order-history-table')).toContainText('WMS_HOLD');
});

test('employee opens employee order details from history', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/employee/order-history/BOG-ORD-020-001');
  await expect(page.getByTestId('employee-order-history-details-page')).toBeVisible();
  await expect(page.getByTestId('employee-order-details-summary')).toContainText('BOG-ORD-020-001');
  await expect(page.getByTestId('employee-order-details-items')).toContainText('SKU-020-CREAM-001');
  await expect(page.getByTestId('employee-order-details-events')).toContainText('PAYMENT_EVENT');
  await expect(page.getByTestId('employee-order-details-audit')).toContainText('ORDER_DETAILS_VIEWED');
});

test('employee uses deep links from employee order details', async ({ page }) => {
  await page.goto('/test-login?role=order-support');
  await expect(page.getByTestId('session-ready')).toContainText('order-support');
  await page.goto('/employee/order-history/BOG-ORD-020-001');
  await page.getByTestId('employee-order-support-link').click();
  await expect(page).toHaveURL(/\/employee\/order-support/);
  await expect(page.getByTestId('employee-order-support-page')).toBeVisible();
  await expect(page.getByTestId('employee-order-timeline')).toContainText('BOG-ORD-020-001');
});

test('supervisor sees extended order history audit', async ({ page }) => {
  await page.goto('/test-login?role=supervisor');
  await expect(page.getByTestId('session-ready')).toContainText('supervisor');
  await page.goto('/employee/order-history/BOG-ORD-020-001');
  await expect(page.getByTestId('employee-order-details-audit')).toContainText('actorRole');
  await expect(page.getByTestId('employee-order-details-audit')).toContainText('supervisorRequired');
});

test('partner cannot open employee order history', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/employee/order-history');
  await expect(page.getByTestId('employee-access-denied')).toContainText('STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
});