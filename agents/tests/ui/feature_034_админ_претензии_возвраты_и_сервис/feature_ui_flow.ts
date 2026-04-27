import { expect, test, type Page } from '@playwright/test';

export async function runFeature034AdminServiceFlow(page: Page) {
  await page.goto('/test-login?role=claim-operator');
  await expect(page.getByTestId('session-ready')).toContainText('claim-operator');

  await page.goto('/admin/service');
  await expect(page.getByTestId('admin-service-page')).toBeVisible();
  await expect(page.getByTestId('admin-service-case-table')).toBeVisible();

  await page.getByTestId('admin-service-search').fill('CLM-034-1001');
  await page.getByTestId('admin-service-sla-status').selectOption('AT_RISK');
  await page.getByTestId('admin-service-claim-type').selectOption('DAMAGED_ITEM');
  await page.getByTestId('admin-service-search-submit').click();
  await expect(page.getByTestId('admin-service-case-table')).toContainText('CLM-034-1001');

  await page.getByTestId('admin-service-open-card-CLM-034-1001').click();
  await expect(page.getByTestId('admin-service-case-card')).toBeVisible();
  await expect(page.getByTestId('admin-service-case-sla')).toContainText('AT_RISK');
  await expect(page.getByTestId('admin-service-audit-trail')).toBeVisible();

  await page.getByTestId('admin-service-take-case').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_SERVICE_CASE_ASSIGNED');

  await page.getByTestId('admin-service-request-info').click();
  await page.getByTestId('admin-service-request-info-message-code').selectOption('STR_MNEMO_ADMIN_SERVICE_REQUEST_PHOTO');
  await page.getByTestId('admin-service-request-info-reason').selectOption('WAITING_CUSTOMER_DATA');
  await page.getByTestId('admin-service-request-info-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_SERVICE_REQUEST_SENT');

  await page.getByTestId('admin-service-tab-decisions').click();
  await page.getByTestId('admin-service-decision-type').selectOption('APPROVE_REFUND');
  await page.getByTestId('admin-service-decision-reason').selectOption('DAMAGED_ITEM_CONFIRMED');
  await page.getByTestId('admin-service-decision-message-code').selectOption('STR_MNEMO_ADMIN_SERVICE_REFUND_APPROVED');
  await page.getByTestId('admin-service-decision-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_SERVICE_DECISION_SAVED');

  await page.getByTestId('admin-service-refund-amount').fill('2500');
  await page.getByTestId('admin-service-refund-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_SERVICE_REFUND_REQUESTED');

  await page.getByTestId('admin-service-tab-replacement').click();
  await page.getByTestId('admin-service-replacement-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-service-replacement-quantity').fill('1');
  await page.getByTestId('admin-service-replacement-warehouse').selectOption('WH-MSK-01');
  await page.getByTestId('admin-service-replacement-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_SERVICE_REPLACEMENT_REQUESTED');

  await page.goto('/test-login?role=service-supervisor');
  await expect(page.getByTestId('session-ready')).toContainText('service-supervisor');
  await page.goto('/admin/service/sla-board');
  await expect(page.getByTestId('admin-service-sla-board')).toBeVisible();
  await expect(page.getByTestId('admin-service-sla-breached-count')).toBeVisible();
  await page.getByTestId('admin-service-open-escalation-CLM-034-1007').click();
  await page.getByTestId('admin-service-supervisor-override').click();
  await page.getByTestId('admin-service-supervisor-override-reason').selectOption('SUPERVISOR_GOODWILL');
  await page.getByTestId('admin-service-supervisor-override-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_SERVICE_OVERRIDE_SAVED');
}

test('admin service manages case queue decision refund replacement and supervisor SLA', async ({ page }) => {
  await runFeature034AdminServiceFlow(page);
});

test('admin service rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/service');
  await expect(page.getByTestId('admin-service-forbidden')).toContainText('STR_MNEMO_ADMIN_SERVICE_ACCESS_DENIED');
});
