import { expect, test, type Page } from '@playwright/test';

export async function runFeature036AdminPlatformFlow(page: Page) {
  await page.goto('/test-login?role=business-admin');
  await expect(page.getByTestId('session-ready')).toContainText('business-admin');
  await page.goto('/admin/platform');
  await expect(page.getByTestId('admin-platform-page')).toBeVisible();
  await expect(page.getByTestId('admin-platform-kpi-board')).toContainText('GMV');
  await expect(page.getByTestId('admin-platform-alerts')).toContainText('STALE_KPI_SOURCE');
  await page.getByTestId('admin-platform-export-format').selectOption('XLSX');
  await page.getByTestId('admin-platform-export-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PLATFORM_EXPORT_STARTED');

  await page.goto('/test-login?role=integration-admin');
  await expect(page.getByTestId('session-ready')).toContainText('integration-admin');
  await page.goto('/admin/platform/integrations');
  await expect(page.getByTestId('admin-platform-integrations')).toContainText('WMS_1C');
  await page.getByTestId('admin-platform-integration-sla').fill('15');
  await page.getByTestId('admin-platform-integration-retry').selectOption('EXPONENTIAL_3');
  await page.getByTestId('admin-platform-integration-reason').fill('SLA_TUNING');
  await page.getByTestId('admin-platform-integration-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED');

  await page.goto('/test-login?role=audit-admin');
  await expect(page.getByTestId('session-ready')).toContainText('audit-admin');
  await page.goto('/admin/platform/audit');
  await page.getByTestId('admin-platform-audit-domain').selectOption('INTEGRATION');
  await page.getByTestId('admin-platform-audit-submit').click();
  await expect(page.getByTestId('admin-platform-audit-table')).toContainText('correlationId');
}

test('admin platform KPI audit and integrations green path', async ({ page }) => {
  await runFeature036AdminPlatformFlow(page);
});

test('admin platform rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/platform');
  await expect(page.getByTestId('admin-platform-forbidden')).toContainText('STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED');
});