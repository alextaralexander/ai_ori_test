// AUTO-GENERATED from agents/tests/. Do not edit manually.
import { expect, test } from '@playwright/test';

test('partner leader opens vip orders list', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');
  await page.goto('/vip-orders');
  await expect(page.getByTestId('partner-offline-orders-page')).toBeVisible();
  await expect(page.getByTestId('partner-offline-order-card-BOG-VIP-017-001')).toContainText('BOG-VIP-017-001');
  await expect(page.getByTestId('partner-offline-order-card-BOG-VIP-017-001')).toContainText('BOG-016-002');
});

test('partner leader opens business order management list', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');
  await page.goto('/business/tools/order-management/vip-orders/partner-orders?campaignId=CAT-2026-05');
  await expect(page.getByTestId('partner-offline-orders-page')).toBeVisible();
  await expect(page.getByTestId('partner-offline-orders-filters')).toBeVisible();
});

test('partner leader opens partner offline order details and repeats order', async ({ page }) => {
  await page.goto('/test-login?role=partner-leader');
  await expect(page.getByTestId('session-ready')).toContainText('partner-leader');
  await page.goto('/business/tools/order-management/vip-orders/partner-orders/BOG-VIP-017-001');
  await expect(page.getByTestId('partner-offline-order-details-page')).toBeVisible();
  await expect(page.getByTestId('partner-offline-order-details-items')).toBeVisible();
  await expect(page.getByTestId('partner-offline-order-details-timeline')).toContainText(/оплата|PAYMENT|STR_MNEMO/i);
  await page.getByTestId('partner-offline-order-repeat').click();
  await expect(page.getByTestId('partner-offline-order-action-result')).toContainText('STR_MNEMO_PARTNER_OFFLINE_ORDER_REPEAT_CREATED');
});

test('customer cannot open partner offline orders', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/vip-orders');
  await expect(page.getByTestId('partner-offline-orders-access-denied')).toContainText(/STR_MNEMO_PARTNER_OFFLINE_ORDER_ACCESS_DENIED|доступ|access/i);
});