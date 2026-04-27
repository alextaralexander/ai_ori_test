// GENERATED FROM agents/tests/ui; DO NOT EDIT MANUALLY.
import { expect, test, type Page } from '@playwright/test';

export async function runFeature030AdminCatalogFlow(page: Page) {
  await page.goto('/test-login?role=catalog-manager');
  await expect(page.getByTestId('session-ready')).toContainText('catalog-manager');

  await page.goto('/admin/catalogs');
  await expect(page.getByTestId('admin-catalog-page')).toBeVisible();
  await expect(page.getByTestId('admin-catalog-campaign-table')).toBeVisible();

  await page.getByTestId('admin-catalog-create-campaign').click();
  await page.getByTestId('admin-catalog-campaign-code').fill('CAM-2026-05');
  await page.getByTestId('admin-catalog-campaign-name').fill('Майский каталог Best Ori Gin');
  await page.getByTestId('admin-catalog-campaign-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CATALOG_CAMPAIGN_SAVED');

  await page.getByTestId('admin-catalog-tab-issues').click();
  await page.getByTestId('admin-catalog-create-issue').click();
  await page.getByTestId('admin-catalog-issue-code').fill('ISSUE-2026-05');
  await page.getByTestId('admin-catalog-issue-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CATALOG_ISSUE_SCHEDULED');

  await page.getByTestId('admin-catalog-tab-materials').click();
  await page.getByTestId('admin-catalog-material-file-name').fill('best-origin-may-2026.pdf');
  await page.getByTestId('admin-catalog-material-checksum').fill('sha256:catalog-may-2026');
  await page.getByTestId('admin-catalog-material-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CATALOG_MATERIAL_SAVED');
  await page.getByTestId('admin-catalog-material-approve').click();
  await expect(page.getByTestId('admin-catalog-material-table')).toContainText('APPROVED');

  await page.getByTestId('admin-catalog-page-image-add').click();
  await expect(page.getByTestId('admin-catalog-page-list')).toContainText('page-1');

  await page.getByTestId('admin-catalog-tab-hotspots').click();
  await page.getByTestId('admin-catalog-hotspot-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-catalog-hotspot-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CATALOG_HOTSPOT_SAVED');
  await page.getByTestId('admin-catalog-validate-links').click();
  await expect(page.getByTestId('admin-catalog-link-report')).toContainText('validHotspots');

  await page.getByTestId('admin-catalog-tab-rollover').click();
  await expect(page.getByTestId('admin-catalog-freeze-warning')).toContainText('STR_MNEMO_ADMIN_CATALOG_FREEZE_WINDOW_ACTIVE');
  await page.getByTestId('admin-catalog-rollover-start').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_CATALOG_ROLLOVER_COMPLETED');
  await expect(page.getByTestId('admin-catalog-archive-list')).toContainText('ISSUE-2026-04');

  await page.getByTestId('admin-catalog-tab-audit').click();
  await expect(page.getByTestId('admin-catalog-audit-table')).toContainText('ROLLOVER_COMPLETED');
}

test('admin catalog publishes campaign PDF and performs rollover', async ({ page }) => {
  await runFeature030AdminCatalogFlow(page);
});

test('admin catalog rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/admin/catalogs');
  await expect(page.getByTestId('admin-catalog-forbidden')).toContainText('STR_MNEMO_ADMIN_CATALOG_FORBIDDEN');
});
