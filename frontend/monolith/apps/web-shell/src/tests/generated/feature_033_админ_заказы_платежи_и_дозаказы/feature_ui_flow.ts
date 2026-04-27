// GENERATED FROM agents/tests/ui/feature_033_админ_заказы_платежи_и_дозаказы/feature_ui_flow.ts. DO NOT EDIT MANUALLY.
import { expect, test, type Page } from '@playwright/test';

export async function runFeature033AdminOrdersFlow(page: Page) {
  await page.goto('/test-login?role=order-admin');
  await expect(page.getByTestId('session-ready')).toContainText('order-admin');

  await page.goto('/admin/orders');
  await expect(page.getByTestId('admin-orders-page')).toBeVisible();
  await expect(page.getByTestId('admin-orders-table')).toBeVisible();

  await page.getByTestId('admin-orders-search').fill('BO-033-1001');
  await page.getByTestId('admin-orders-payment-status').selectOption('PAID');
  await page.getByTestId('admin-orders-search-submit').click();
  await expect(page.getByTestId('admin-orders-table')).toContainText('BO-033-1001');

  await page.getByTestId('admin-orders-open-card-BO-033-1001').click();
  await expect(page.getByTestId('admin-order-card')).toBeVisible();
  await expect(page.getByTestId('admin-order-payment-timeline')).toBeVisible();
  await expect(page.getByTestId('admin-order-audit-trail')).toBeVisible();

  await page.getByTestId('admin-order-create-supplementary').click();
  await page.getByTestId('admin-order-supplementary-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-order-supplementary-quantity').fill('1');
  await page.getByTestId('admin-order-supplementary-reason').selectOption('CUSTOMER_REQUEST');
  await page.getByTestId('admin-order-supplementary-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_ORDER_SUPPLEMENTARY_CREATED');

  await page.getByTestId('admin-order-tab-payments').click();
  await expect(page.getByTestId('admin-order-payment-timeline')).toContainText('CAPTURE');
  await page.getByTestId('admin-order-create-refund').click();
  await page.getByTestId('admin-order-refund-amount').fill('2500');
  await page.getByTestId('admin-order-refund-reason').selectOption('PARTIAL_CANCEL');
  await page.getByTestId('admin-order-refund-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_ORDER_REFUND_REQUESTED');

  await page.getByTestId('admin-order-tab-risk').click();
  await expect(page.getByTestId('admin-order-risk-panel')).toBeVisible();
  await page.getByTestId('admin-order-risk-decision').selectOption('APPROVED');
  await page.getByTestId('admin-order-risk-reason').selectOption('MANUAL_REVIEW_PASSED');
  await page.getByTestId('admin-order-risk-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_ORDER_RISK_DECISION_SAVED');

  await page.getByTestId('admin-order-tab-audit').click();
  await expect(page.getByTestId('admin-order-audit-table')).toContainText('ADMIN_ORDER');
}

test('admin orders manages order card supplementary payment refund and risk', async ({ page }) => {
  await runFeature033AdminOrdersFlow(page);
});

test('admin orders rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/orders');
  await expect(page.getByTestId('admin-orders-forbidden')).toContainText('STR_MNEMO_ADMIN_ORDER_ACCESS_DENIED');
});
