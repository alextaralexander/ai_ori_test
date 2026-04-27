import { expect, test } from '@playwright/test';

test('customer searches order history and opens order details', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order/order-history');
  await expect(page.getByTestId('order-history-page')).toBeVisible();
  await page.getByTestId('order-history-search').fill('ORD-011-MAIN');
  await page.getByTestId('order-history-submit').click();
  await expect(page.getByTestId('order-history-card-ORD-011-MAIN')).toBeVisible();
  await expect(page.getByTestId('order-history-card-ORD-011-MAIN')).toContainText(/ORD-011-MAIN|оплачен|paid/i);
  await page.getByTestId('order-history-details-ORD-011-MAIN').click();
  await expect(page).toHaveURL(/\/order\/order-history\/ORD-011-MAIN/);
  await expect(page.getByTestId('order-details-page')).toBeVisible();
  await expect(page.getByTestId('order-details-items')).toBeVisible();
  await expect(page.getByTestId('order-details-timeline')).toBeVisible();
  await expect(page.getByTestId('order-details-repeat')).toBeVisible();
});

test('customer continues pending payment from order details', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order/order-history/ORD-011-PAY');
  await expect(page.getByTestId('order-details-page')).toBeVisible();
  await expect(page.getByTestId('order-payment-warning')).toContainText(/STR_MNEMO_ORDER_PAYMENT_PENDING|оплат|payment/i);
  await page.getByTestId('order-details-pay').click();
  await expect(page.getByTestId('order-payment-action-result')).toBeVisible();
});

test('partner sees supplementary order separately', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/order/order-history?orderType=SUPPLEMENTARY');
  await expect(page.getByTestId('order-history-page')).toBeVisible();
  await expect(page.getByTestId('order-history-card-ORD-011-SUPP')).toContainText('SUPPLEMENTARY');
  await page.getByTestId('order-history-details-ORD-011-SUPP').click();
  await expect(page.getByTestId('order-details-partner-benefits')).toBeVisible();
  await expect(page.getByTestId('order-details-repeat-supplementary')).toBeVisible();
});

test('foreign order is not exposed', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order/order-history/ORD-011-OTHER');
  await expect(page.getByTestId('order-history-access-denied')).toContainText(/STR_MNEMO_ORDER_HISTORY_ACCESS_DENIED|доступ|access/i);
  await expect(page.getByTestId('order-details-page')).toHaveCount(0);
});

test('order history layout is usable on mobile width', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 900 });
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/order/order-history');
  await expect(page.getByTestId('order-history-page')).toBeVisible();
  await expect(page.getByTestId('order-history-mobile-filters')).toBeVisible();
  await expect(page.getByTestId('order-history-card-ORD-011-MAIN')).toBeVisible();
  const horizontalOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth);
  expect(horizontalOverflow).toBe(false);
});
