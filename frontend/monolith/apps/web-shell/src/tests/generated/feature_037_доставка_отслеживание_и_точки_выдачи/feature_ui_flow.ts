// GENERATED FROM agents/tests/ui. DO NOT EDIT MANUALLY.
import { expect, test, type Page } from '@playwright/test';

export async function runFeature037DeliveryFlow(page: Page) {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/checkout');
  await expect(page.getByTestId('checkout-delivery-section')).toBeVisible();
  await page.getByTestId('delivery-method-pickup-point').click();
  await page.getByTestId('pickup-point-search-input').fill('Moscow');
  await page.getByTestId('pickup-point-search-submit').click();
  await expect(page.getByTestId('pickup-point-card')).toContainText('storageLimitDays');
  await page.getByTestId('pickup-point-select').click();
  await page.getByTestId('checkout-confirm-order').click();
  await expect(page.getByTestId('delivery-notification-root')).toContainText('STR_MNEMO_DELIVERY_SHIPMENT_CREATED');

  await page.goto('/orders/00000000-0000-0000-0000-000000000137');
  await expect(page.getByTestId('order-tracking-timeline')).toContainText('READY_FOR_PICKUP');
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_DELIVERY_READY_FOR_PICKUP');

  await page.goto('/test-login?role=pickup-owner');
  await expect(page.getByTestId('session-ready')).toContainText('pickup-owner');
  await page.goto('/pickup-owner/shipments');
  await expect(page.getByTestId('pickup-owner-shipment-table')).toContainText('ARRIVED_AT_PICKUP_POINT');
  await page.getByTestId('pickup-shipment-accept').click();
  await expect(page.getByTestId('pickup-owner-shipment-table')).toContainText('READY_FOR_PICKUP');
  await page.getByTestId('pickup-verification-code').fill('037037');
  await page.getByTestId('pickup-shipment-deliver').click();
  await expect(page.getByTestId('pickup-owner-shipment-table')).toContainText('DELIVERED');

  await page.goto('/test-login?role=delivery-operator');
  await expect(page.getByTestId('session-ready')).toContainText('delivery-operator');
  await page.goto('/delivery/operator/journal?correlationId=CORR-037');
  await expect(page.getByTestId('delivery-operator-journal')).toContainText('correlationId');
}

test('delivery tracking and pickup points green path', async ({ page }) => {
  await runFeature037DeliveryFlow(page);
});

test('delivery tracking rejects customer without order access', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/orders/not-owned-order/tracking');
  await expect(page.getByTestId('delivery-forbidden')).toContainText('STR_MNEMO_DELIVERY_ACCESS_DENIED');
});
