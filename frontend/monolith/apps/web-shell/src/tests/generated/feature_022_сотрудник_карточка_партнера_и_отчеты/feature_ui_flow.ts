// Generated from agents/tests/. Do not edit manually.
import { expect, test } from '@playwright/test';

test('employee opens partner card with kpi and linked routes', async ({ page }) => {
  await page.goto('/test-login?role=backoffice');
  await expect(page.getByTestId('session-ready')).toContainText('backoffice');
  await page.goto('/employee/partner-card?query=P-022-7788');
  await expect(page.getByTestId('employee-partner-card-page')).toBeVisible();
  await expect(page.getByTestId('employee-partner-card-search')).toBeVisible();
  await expect(page.getByTestId('employee-partner-card-summary')).toContainText('P-022-7788');
  await expect(page.getByTestId('employee-partner-card-kpi')).toContainText('personalVolume');
  await expect(page.getByTestId('employee-partner-order-row-BOG-ORD-022-001')).toContainText('OPEN_CLAIM');
});

test('regional manager filters partner order report', async ({ page }) => {
  await page.goto('/test-login?role=regional-manager');
  await expect(page.getByTestId('session-ready')).toContainText('regional-manager');
  await page.goto('/employee/report/order-history?partnerId=PART-022-001&problemOnly=true&regionCode=RU-MOW');
  await expect(page.getByTestId('employee-partner-report-page')).toBeVisible();
  await expect(page.getByTestId('employee-partner-report-table')).toContainText('BOG-ORD-022-001');
  await expect(page.getByTestId('employee-partner-report-table')).toContainText('OPEN_CLAIM');
  await expect(page.getByTestId('employee-partner-report-aggregate')).toContainText('totalOrders');
});

test('guest cannot open employee partner card', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/employee/partner-card?query=P-022-7788');
  await expect(page.getByTestId('employee-access-denied')).toContainText('STR_MNEMO_EMPLOYEE_ACCESS_DENIED');
});