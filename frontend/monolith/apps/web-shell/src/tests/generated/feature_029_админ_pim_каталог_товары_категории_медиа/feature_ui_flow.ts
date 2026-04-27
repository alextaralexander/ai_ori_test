// GENERATED FROM agents/tests/ui; DO NOT EDIT MANUALLY.
import { expect, test, type Page } from '@playwright/test';

export async function runFeature029AdminPimFlow(page: Page) {
  await page.goto('/test-login?role=pim-manager');
  await expect(page.getByTestId('session-ready')).toContainText('pim-manager');

  await page.goto('/admin/pim');
  await expect(page.getByTestId('admin-pim-page')).toBeVisible();
  await expect(page.getByTestId('admin-pim-product-table')).toBeVisible();

  await page.getByTestId('admin-pim-tab-categories').click();
  await page.getByTestId('admin-pim-create-category').click();
  await page.getByTestId('admin-pim-category-slug').fill('face-care');
  await page.getByTestId('admin-pim-category-name').fill('Уход за лицом');
  await page.getByTestId('admin-pim-category-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_CATEGORY_SAVED');
  await page.getByTestId('admin-pim-category-activate').click();
  await expect(page.getByTestId('admin-pim-category-tree')).toContainText('ACTIVE');

  await page.getByTestId('admin-pim-tab-products').click();
  await page.getByTestId('admin-pim-create-product').click();
  await page.getByTestId('admin-pim-product-sku').fill('BOG-SERUM-001');
  await page.getByTestId('admin-pim-product-article-code').fill('SRM-001');
  await page.getByTestId('admin-pim-product-brand-code').fill('BEST_ORI_GIN');
  await page.getByTestId('admin-pim-product-name').fill('Сыворотка сияние');
  await page.getByTestId('admin-pim-product-description').fill('Glow serum');
  await page.getByTestId('admin-pim-product-composition').fill('Water, niacinamide');
  await page.getByTestId('admin-pim-product-category-face-care').click();
  await page.getByTestId('admin-pim-product-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_PRODUCT_SAVED');

  await page.getByTestId('admin-pim-product-publish').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_PRODUCT_MAIN_IMAGE_REQUIRED');

  await page.getByTestId('admin-pim-tab-media').click();
  await page.getByTestId('admin-pim-media-add').click();
  await page.getByTestId('admin-pim-media-file-name').fill('serum-main.jpg');
  await page.getByTestId('admin-pim-media-mime-type').fill('image/jpeg');
  await page.getByTestId('admin-pim-media-checksum').fill('sha256:serum-main');
  await page.getByTestId('admin-pim-media-alt-text').fill('Флакон сыворотки Best Ori Gin');
  await page.getByTestId('admin-pim-media-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_MEDIA_SAVED');
  await page.getByTestId('admin-pim-media-approve').click();
  await expect(page.getByTestId('admin-pim-media-table')).toContainText('APPROVED');

  await page.getByTestId('admin-pim-tab-products').click();
  await page.getByTestId('admin-pim-product-publish').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_PRODUCT_PUBLISHED');
  await expect(page.getByTestId('admin-pim-product-table')).toContainText('PUBLISHED');

  await page.getByTestId('admin-pim-tab-import-export').click();
  await page.getByTestId('admin-pim-import-start').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_IMPORT_APPLIED');
  await page.getByTestId('admin-pim-export-start').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PIM_EXPORT_CREATED');

  await page.getByTestId('admin-pim-tab-audit').click();
  await expect(page.getByTestId('admin-pim-audit-table')).toContainText('correlationId');
}

test('admin PIM publishes product with category, media and import export', async ({ page }) => {
  await runFeature029AdminPimFlow(page);
});

test('admin PIM rejects forbidden user', async ({ page }) => {
  await page.goto('/test-login?role=employee-support');
  await expect(page.getByTestId('session-ready')).toContainText('employee-support');
  await page.goto('/admin/pim');
  await expect(page.getByTestId('admin-pim-forbidden')).toContainText('STR_MNEMO_ADMIN_PIM_FORBIDDEN');
});
