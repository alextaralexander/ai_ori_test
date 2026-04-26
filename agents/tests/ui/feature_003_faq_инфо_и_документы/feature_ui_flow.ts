import { expect, test } from '@playwright/test';

test('guest searches FAQ and opens delivery info section', async ({ page }) => {
  await page.goto('/FAQ');
  await expect(page.getByTestId('faq-page')).toBeVisible();
  await page.getByTestId('faq-search').fill('delivery');
  await expect(page.getByTestId('faq-item-delivery-time')).toBeVisible();
  await page.getByTestId('faq-related-info-delivery').click();
  await expect(page).toHaveURL(/\/info\/delivery/);
  await expect(page.getByTestId('info-page')).toBeVisible();
  await expect(page.getByTestId('info-related-document-terms')).toBeVisible();
});

test('customer opens terms documents with PDF viewer and archive', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/documents/terms');
  await expect(page.getByTestId('documents-page')).toBeVisible();
  await expect(page.getByTestId('document-current-user-terms')).toBeVisible();
  await expect(page.getByTestId('document-viewer-user-terms')).toBeVisible();
  await expect(page.getByTestId('document-archive-user-terms')).toBeVisible();
});

test('partner sees partner documents and required marker', async ({ page }) => {
  await page.goto('/test-login?role=partner');
  await expect(page.getByTestId('session-ready')).toContainText('partner');
  await page.goto('/documents/partner');
  await expect(page.getByTestId('documents-page')).toBeVisible();
  await expect(page.getByTestId('document-current-partner-agreement')).toBeVisible();
  await expect(page.getByTestId('document-required-partner-agreement')).toBeVisible();
});
