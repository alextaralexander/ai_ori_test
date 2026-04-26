import { expect, test } from '@playwright/test';

test('customer opens current digital catalogue and navigates to product card', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/products/digital-catalogue-current');
  await expect(page.getByTestId('digital-catalogue-page')).toBeVisible();
  await expect(page.getByTestId('digital-catalogue-period')).toBeVisible();
  await expect(page.getByTestId('digital-catalogue-viewer')).toBeVisible();
  await expect(page.getByTestId('digital-catalogue-materials')).toBeVisible();
  await page.getByTestId('digital-catalogue-next-page').click();
  await expect(page.getByTestId('digital-catalogue-page-number')).toBeVisible();
  await page.getByTestId('digital-catalogue-hotspot-BOG-CREAM-001').click();
  await expect(page).toHaveURL(/\/product\/BOG-CREAM-001/);
});

test('customer uses pdf viewer actions for allowed material', async ({ page }) => {
  await page.goto('/test-login?role=customer');
  await expect(page.getByTestId('session-ready')).toContainText('customer');
  await page.goto('/products/digital-catalogue-current');
  await page.getByTestId('digital-catalogue-material-catalog-current-pdf').click();
  await page.getByTestId('digital-catalogue-zoom-in').click();
  await page.getByTestId('digital-catalogue-zoom-out').click();
  await page.getByTestId('digital-catalogue-download').click();
  await expect(page.getByTestId('digital-catalogue-action-message')).toContainText(/готов|ready|download/i);
  await page.getByTestId('digital-catalogue-share').click();
  await expect(page.getByTestId('digital-catalogue-share-url')).toBeVisible();
});

test('guest sees restricted next catalogue localized state', async ({ page }) => {
  await page.goto('/test-login?role=guest');
  await expect(page.getByTestId('session-ready')).toContainText('guest');
  await page.goto('/products/digital-catalogue-next?preview=false');
  await expect(page.getByTestId('digital-catalogue-forbidden')).toBeVisible();
  await expect(page.getByTestId('digital-catalogue-forbidden')).toContainText(/недоступ|forbidden|not available/i);
  await expect(page.getByTestId('digital-catalogue-viewer')).toHaveCount(0);
});

test('content manager previews catalogue materials without public draft leak', async ({ page }) => {
  await page.goto('/test-login?role=content-manager');
  await expect(page.getByTestId('session-ready')).toContainText('content-manager');
  await page.goto('/products/digital-catalogue-current?preview=true');
  await expect(page.getByTestId('digital-catalogue-materials')).toBeVisible();
  await expect(page.getByTestId('digital-catalogue-draft-public-link')).toHaveCount(0);
});
