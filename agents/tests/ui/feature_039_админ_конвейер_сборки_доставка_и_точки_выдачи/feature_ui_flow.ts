import { expect, type Page } from '@playwright/test';

export async function runFeature039AdminFulfillmentFlow(page: Page): Promise<void> {
  await page.goto('/test-login?role=fulfillment-admin');
  await expect(page.getByTestId('session-ready')).toContainText('fulfillment-admin');

  await page.goto('/admin/fulfillment');
  await expect(page.getByTestId('admin-fulfillment-page')).toBeVisible();
  await page.getByTestId('fulfillment-filter-correlation-id').fill('CORR-039-1');
  await page.getByTestId('fulfillment-filter-apply').click();
  await expect(page.getByTestId('fulfillment-dashboard-table')).toBeVisible();

  await page.goto('/test-login?role=conveyor-operator');
  await expect(page.getByTestId('session-ready')).toContainText('conveyor-operator');
  await page.goto('/admin/fulfillment/conveyor');
  await page.getByTestId('conveyor-task-search').fill('FUL-039-001');
  await page.getByTestId('conveyor-task-open').click();
  await page.getByTestId('conveyor-scan-order').fill('BO-E2E-039-1');
  await page.getByTestId('conveyor-stage-pick').click();
  await expect(page.getByTestId('conveyor-current-stage')).toContainText('PICK_IN_PROGRESS');
  await page.getByTestId('conveyor-stage-pack').click();
  await expect(page.getByTestId('conveyor-current-stage')).toContainText('PACK_IN_PROGRESS');
  await page.getByTestId('conveyor-stage-sort').click();
  await expect(page.getByTestId('conveyor-current-stage')).toContainText('SORT_PENDING');

  await page.goto('/test-login?role=delivery-admin');
  await expect(page.getByTestId('session-ready')).toContainText('delivery-admin');
  await page.goto('/admin/fulfillment/delivery-services');
  await page.getByTestId('delivery-service-create').click();
  await page.getByTestId('delivery-service-code').fill('DELIVERY_039');
  await page.getByTestId('delivery-service-display-key').fill('adminFulfillment.delivery.delivery039');
  await page.getByTestId('delivery-service-endpoint-alias').fill('delivery-provider-039');
  await page.getByTestId('delivery-service-save').click();
  await page.getByTestId('delivery-service-activate').click();
  await expect(page.getByTestId('delivery-service-status')).toContainText('ACTIVE');

  await page.goto('/test-login?role=pickup-network-admin');
  await expect(page.getByTestId('session-ready')).toContainText('pickup-network-admin');
  await page.goto('/admin/fulfillment/pickup-points');
  await page.getByTestId('pickup-point-create').click();
  await page.getByTestId('pickup-point-code').fill('PVZ-039-001');
  await page.getByTestId('pickup-point-owner').fill('OWNER-039');
  await page.getByTestId('pickup-point-address').fill('Москва, Тестовая улица, 39');
  await page.getByTestId('pickup-point-storage-limit').fill('7');
  await page.getByTestId('pickup-point-shipment-limit').fill('120');
  await page.getByTestId('pickup-point-save').click();
  await page.getByTestId('pickup-point-activate').click();
  await expect(page.getByTestId('pickup-point-status')).toContainText('ACTIVE');
  await page.getByTestId('pickup-point-temporary-close').click();
  await page.getByTestId('pickup-point-reason-code').fill('OVERLOAD');
  await page.getByTestId('pickup-point-confirm-close').click();
  await expect(page.getByTestId('pickup-point-status')).toContainText('TEMPORARILY_CLOSED');

  await page.goto('/test-login?role=pickup-owner');
  await expect(page.getByTestId('session-ready')).toContainText('pickup-owner');
  await page.goto('/admin/fulfillment/pickup-owner');
  await page.getByTestId('pickup-shipment-search').fill('SHP-E2E-039-3');
  await page.getByTestId('pickup-shipment-accept').click();
  await expect(page.getByTestId('pickup-shipment-status')).toContainText('ACCEPTED');
  await page.getByTestId('pickup-shipment-not-collected').click();
  await page.getByTestId('pickup-shipment-reason-code').fill('STORAGE_EXPIRED');
  await page.getByTestId('pickup-shipment-confirm-not-collected').click();
  await expect(page.getByTestId('pickup-shipment-status')).toContainText('NOT_COLLECTED');
}
