// Synchronized from agents/tests. Do not edit this generated runtime copy manually.
import { expect, test, type Page } from '@playwright/test';

export async function runFeature032AdminWmsFlow(page: Page) {
  await page.goto('/test-login?role=logistics-admin');
  await expect(page.getByTestId('session-ready')).toContainText('logistics-admin');
  await page.goto('/admin/wms');
  await expect(page.getByTestId('admin-wms-page')).toBeVisible();
  await expect(page.getByTestId('admin-wms-warehouse-table')).toBeVisible();

  await page.getByTestId('admin-wms-create-warehouse').click();
  await page.getByTestId('admin-wms-warehouse-code').fill('WH-MSK-01');
  await page.getByTestId('admin-wms-warehouse-name').fill('Московский склад');
  await page.getByTestId('admin-wms-warehouse-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_WMS_WAREHOUSE_SAVED');

  await page.getByTestId('admin-wms-tab-stocks').click();
  await page.getByTestId('admin-wms-stock-sku-filter').fill('BOG-SERUM-001');
  await page.getByTestId('admin-wms-stock-search').click();
  await expect(page.getByTestId('admin-wms-stock-table')).toContainText('BOG-SERUM-001');
  await page.getByTestId('admin-wms-change-availability').click();
  await page.getByTestId('admin-wms-availability-policy').selectOption('SELLABLE');
  await page.getByTestId('admin-wms-availability-reason').fill('CATALOG_READY');
  await page.getByTestId('admin-wms-availability-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_WMS_AVAILABILITY_SAVED');

  await page.getByTestId('admin-wms-tab-supplies').click();
  await page.getByTestId('admin-wms-create-supply').click();
  await page.getByTestId('admin-wms-supply-code').fill('SUP-032-001');
  await page.getByTestId('admin-wms-supply-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-wms-supply-planned-qty').fill('120');
  await page.getByTestId('admin-wms-supply-save').click();
  await page.getByTestId('admin-wms-supply-accept').click();
  await page.getByTestId('admin-wms-accepted-qty').fill('118');
  await page.getByTestId('admin-wms-damaged-qty').fill('2');
  await page.getByTestId('admin-wms-acceptance-reason').fill('DAMAGED_IN_TRANSIT');
  await page.getByTestId('admin-wms-acceptance-confirm').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_WMS_SUPPLY_ACCEPTED');

  await page.getByTestId('admin-wms-tab-sync').click();
  await page.getByTestId('admin-wms-sync-source').selectOption('WMS');
  await page.getByTestId('admin-wms-sync-start').click();
  await expect(page.getByTestId('admin-wms-sync-journal')).toBeVisible();

  await page.getByTestId('admin-wms-tab-audit').click();
  await expect(page.getByTestId('admin-wms-audit-table')).toContainText('ADMIN_WMS');
}

test('admin WMS manages warehouses stocks supplies and sync', async ({ page }) => {
  await runFeature032AdminWmsFlow(page);
});

test('admin WMS rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/wms');
  await expect(page.getByTestId('admin-wms-forbidden')).toContainText('STR_MNEMO_ADMIN_WMS_ACCESS_DENIED');
});
